package org.fdroid.fdroid.updater;

import android.content.Context;
import android.util.Log;

import com.ruptech.k_app.R;

import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.Hasher;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.data.Repo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SignedRepoUpdater extends RepoUpdater {

    private static final String TAG = "org.fdroid.fdroid.updater.SignedRepoUpdater";

    public SignedRepoUpdater(Context ctx) {
        super(ctx);
    }

    private boolean verifyCerts(JarEntry item) throws UpdateException {
        Certificate[] certs = item.getCertificates();
        if (certs == null || certs.length == 0) {
            throw new UpdateException( "No signature found in index");
        }

        Log.d(TAG, "Index has " + certs.length + " signature(s)");
        boolean match = false;
        for (Certificate cert : certs) {
            String certdata = Hasher.hex(cert);
            if (Repo.pubkey == null && Repo.fingerprint != null) {
                String certFingerprint = Utils.calcFingerprint(cert);
                Log.d(TAG, "No public key for repo " + Repo.address + " yet, but it does have a fingerprint, so comparing them.");
                Log.d(TAG, "Repo fingerprint: " + Repo.fingerprint);
                Log.d(TAG, "Cert fingerprint: " + certFingerprint);
                if (Repo.fingerprint.equalsIgnoreCase(certFingerprint)) {
                    Repo.pubkey = certdata;
                    usePubkeyInJar = true;
                }
            }
            if (Repo.pubkey != null && Repo.pubkey.equals(certdata)) {
                Log.d(TAG, "Checking repo public key against cert found in jar.");
                match = true;
                break;
            }
        }
        return match;
    }

    protected File extractIndexFromJar(File indexJar) throws UpdateException {
        File indexFile = null;
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(indexJar, true);
            JarEntry indexEntry = (JarEntry) jarFile.getEntry("index.xml");

            indexFile = File.createTempFile("index-", ".xml", context.getFilesDir());
            InputStream input = null;
            OutputStream output = null;
            try {
                /*
                 * JarFile.getInputStream() provides the signature check, even
                 * though the Android docs do not mention this, the Java docs do
                 * and Android seems to implement it the same:
                 * http://docs.oracle.com/javase/6/docs/api/java/util/jar/JarFile.html#getInputStream(java.util.zip.ZipEntry)
                 * https://developer.android.com/reference/java/util/jar/JarFile.html#getInputStream(java.util.zip.ZipEntry)
                 */
                input = jarFile.getInputStream(indexEntry);
                output = new FileOutputStream(indexFile);
                Utils.copy(input, output);
            } finally {
                Utils.closeQuietly(output);
                Utils.closeQuietly(input);
            }

            // Can only read certificates from jar after it has been read
            // completely, so we put it after the copy above...
            if (!verifyCerts(indexEntry)) {
                indexFile.delete();
                throw new UpdateException("Index signature mismatch");
            }
        } catch (IOException e) {
            if (indexFile != null) {
                indexFile.delete();
            }
            throw new UpdateException(
                    "Error opening signed index", e);
        } finally {
            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException ioe) {
                    // ignore
                }
            }
        }

        return indexFile;
    }

    @Override
    protected String getIndexAddress() {
        return Repo.address + "/index.jar?client_version=" + context.getString(R.string.version_name);
    }

    /**
     * As this is a signed repo - we download the jar file,
     * check the signature, and extract the index file
     */
    @Override
    protected File getIndexFromFile(File downloadedFile) throws
            UpdateException {
        Date updateTime = new Date(System.currentTimeMillis());
        Log.d(TAG, "Getting signed index from " + Repo.address + " at " +
                Utils.LOG_DATE_FORMAT.format(updateTime));

        File indexJar = downloadedFile;
        File indexXml = null;

        // Don't worry about checking the status code for 200. If it was a
        // successful download, then we will have a file ready to use:
        if (indexJar != null && indexJar.exists()) {

            // Due to a bug in android 5.0 lollipop, the inclusion of BouncyCastle causes
            // breakage when verifying the signature of the downloaded .jar. For more
            // details, check out https://gitlab.com/fdroid/fdroidclient/issues/111.
            try {
                FDroidApp.disableSpongyCastleOnLollipop();
                indexXml = extractIndexFromJar(indexJar);
            } finally {
                FDroidApp.enableSpongyCastleOnLollipop();
            }

        }
        return indexXml;
    }
}
