package org.fdroid.fdroid.updater;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.fdroid.fdroid.ProgressListener;
import org.fdroid.fdroid.RepoXMLHandler;
import org.fdroid.fdroid.Utils;
import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.data.Repo;
import org.fdroid.fdroid.net.Downloader;
import org.fdroid.fdroid.net.DownloaderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

abstract public class RepoUpdater implements Updater {

    public static final String PROGRESS_TYPE_PROCESS_XML = "processingXml";

    public static final String PROGRESS_DATA_REPO_ADDRESS = "repoAddress";

    public static Updater createUpdaterFor(Context ctx) {
//        if (Repo.fingerprint == null && Repo.pubkey == null) {
//            return new UnsignedRepoUpdater(ctx);
//        } else {
//            return new SignedRepoUpdater(ctx);
//        }
        return new KAppUpdater(ctx);
    }

    protected final Context context;
    private List<App> apps = new ArrayList<App>();
    private List<Apk> apks = new ArrayList<Apk>();
    protected boolean usePubkeyInJar = false;
    protected boolean hasChanged = false;
    protected ProgressListener progressListener;

    public RepoUpdater(Context ctx) {
        this.context = ctx;
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    public List<App> getApps() {
        return apps;
    }

    public List<Apk> getApks() {
        return apks;
    }

    /**
     * For example, you may want to unzip a jar file to get the index inside,
     * or if the file is not compressed, you can just return a reference to
     * the downloaded file.
     *
     * @throws org.fdroid.fdroid.updater.UpdateException All error states will come from here.
     */
    protected abstract File getIndexFromFile(File downloadedFile) throws
            UpdateException;

    protected abstract String getIndexAddress();

    protected Downloader downloadIndex() throws UpdateException {
        Downloader downloader = null;
        try {
            downloader = DownloaderFactory.create(getIndexAddress(), context);
            downloader.setCacheTag(Repo.lastetag);

            if (progressListener != null) { // interactive session, show progress
                Bundle data = new Bundle(1);
                data.putString(PROGRESS_DATA_REPO_ADDRESS, Repo.address);
                downloader.setProgressListener(progressListener, data);
            }

            downloader.downloadUninterrupted();

            if (downloader.isCached()) {
                // The index is unchanged since we last read it. We just mark
                // everything that came from this repo as being updated.
                Log.d("FDroid", "Repo index for " + getIndexAddress()
                        + " is up to date (by etag)");
            }

        } catch (IOException e) {
            if (downloader != null && downloader.getFile() != null) {
                downloader.getFile().delete();
            }
            throw new UpdateException(
                    "Error getting index file from " + Repo.address,
                    e);
        }
        return downloader;
    }

    private int estimateAppCount(File indexFile) {
        int count = -1;
        try {
            // A bit of a hack, this might return false positives if an apps description
            // or some other part of the XML file contains this, but it is a pretty good
            // estimate and makes the progress counter more informative.
            // As with asking the server about the size of the index before downloading,
            // this also has a time tradeoff. It takes about three seconds to iterate
            // through the file and count 600 apps on a slow emulator (v17), but if it is
            // taking two minutes to update, the three second wait may be worth it.
            final String APPLICATION = "<application";
            count = Utils.countSubstringOccurrence(indexFile, APPLICATION);
        } catch (IOException e) {
            // Do nothing. Leave count at default -1 value.
        }
        return count;
    }


    public void update() throws UpdateException {

        File downloadedFile = null;
        File indexFile = null;
        try {

            Downloader downloader = downloadIndex();
            hasChanged = downloader.hasChanged();

            if (hasChanged) {

                downloadedFile = downloader.getFile();
                indexFile = getIndexFromFile(downloadedFile);

                // Process the index...
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                XMLReader reader = parser.getXMLReader();
                RepoXMLHandler handler = new RepoXMLHandler(progressListener);

                if (progressListener != null) {
                    // Only bother spending the time to count the expected apps
                    // if we can show that to the user...
                    handler.setTotalAppCount(estimateAppCount(indexFile));
                }

                reader.setContentHandler(handler);
                InputSource is = new InputSource(
                        new BufferedReader(new FileReader(indexFile)));

                reader.parse(is);
                apps = handler.getApps();
                apks = handler.getApks();

            }
        } catch (SAXException e) {
            throw new UpdateException(
                    "Error parsing index for repo " + Repo.address, e);
        } catch (FileNotFoundException e) {
            throw new UpdateException(
                    "Error parsing index for repo " + Repo.address, e);
        } catch (ParserConfigurationException e) {
            throw new UpdateException(
                    "Error parsing index for repo " + Repo.address, e);
        } catch (IOException e) {
            throw new UpdateException(
                    "Error parsing index for repo " + Repo.address, e);
        } finally {
            if (downloadedFile != null &&
                    downloadedFile != indexFile && downloadedFile.exists()) {
                downloadedFile.delete();
            }
            if (indexFile != null && indexFile.exists()) {
                indexFile.delete();
            }

        }
    }


}
