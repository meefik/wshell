Build Shell In A Box
====================

In QEMU chroot environment (qemu-arm-static):

1) Get Shell In A Box:

```sh
apt-get build-dep shellinabox
apt-get source shellinabox
```

2) Apply patches:

```sh
cd shellinabox-2.14/shellinabox
patch -Np1 < ../../wShell/contrib/shellinabox.diff
```

3) Build Shell In A Box:

```sh
cd ..
./configure --disable-runtime-loading --disable-pam
make
gcc -g -std=gnu99 -Wall -o shellinaboxd shellinaboxd.o externalfile.o launcher.o privileges.o service.o session.o usercss.o  ./.libs/liblogging.a ./.libs/libhttp.a -lz -lssl -lcrypto -lutil -ldl -lz -static
strip -s shellinaboxd
```

4) Copy shellinaboxd binary to wShell directory:

```sh
cp shellinaboxd ../wShell/app/src/main/assets/shellinaboxd
```

