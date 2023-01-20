#! /bin/bash
#
# /*
#  * Copyright (c) 2023  Bitshift D.O.O (http://bitshifted.co)
#  *
#  * This Source Code Form is subject to the terms of the Mozilla Public
#  * License, v. 2.0. If a copy of the MPL was not distributed with this
#  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
#  */
JDK_RELEASE=17.0.3+17
JDK_TARBALL=jdk-17.tar.gz
TARGET_DIR=target/docker/jdk
mkdir -p $TARGET_DIR

wget https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jdk_x64_linux_hotspot_17.0.3_7.tar.gz -O $TARGET_DIR/$JDK_TARBALL
cd $TARGET_DIR
tar xvzf $JDK_TARBALL
rm $JDK_TARBALL
