Build Shell In A Box
====================

In QEMU chroot environment (qemu-arm-static):

1) Get Shell In A Box:

    wget http://shellinabox.googlecode.com/files/shellinabox-2.14.tar.gz
    tar xzf shellinabox-2.14.tar.gz

2) Apply patches:

    cd shellinabox-2.14/shellinabox
    patch -Np1 < ../../wShell/contrib/shellinabox.diff

3) Build Shell In A Box:

    cd ..
    ./configure --disable-runtime-loading
    make CFLAGS="-lssl -lcrypto -ldl -static" LDFLAGS="-static"
    gcc -g -std=gnu99 -Wall -o shellinaboxd shellinaboxd.o externalfile.o launcher.o privileges.o service.o session.o usercss.o  ./.libs/liblogging.a ./.libs/libhttp.a -lssl -lcrypto -ldl -lz -lutil -static
    strip -s shellinaboxd

4) Copy shellinaboxd binary to wShell directory:

    cp shellinaboxd ../wShell/assets/shellinaboxd

