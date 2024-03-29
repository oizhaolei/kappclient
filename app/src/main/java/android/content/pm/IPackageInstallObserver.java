/*
 * Copyright (C) 2013 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package android.content.pm;

/**
 * Just a non-working implementation of this Stub to satisfy compiler!
 */
public interface IPackageInstallObserver extends android.os.IInterface {

    public abstract static class Stub extends android.os.Binder implements
            IPackageInstallObserver {
        public Stub() {
            throw new RuntimeException("Stub!");
        }

        public static IPackageInstallObserver asInterface(android.os.IBinder obj) {
            throw new RuntimeException("Stub!");
        }

        public android.os.IBinder asBinder() {
            throw new RuntimeException("Stub!");
        }

        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply,
                                  int flags) throws android.os.RemoteException {
            throw new RuntimeException("Stub!");
        }
    }

    public abstract void packageInstalled(String packageName, int returnCode)
            throws android.os.RemoteException;
}