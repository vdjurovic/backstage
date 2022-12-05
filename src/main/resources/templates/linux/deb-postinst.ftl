#
# /*
#  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
#  *
#  * This Source Code Form is subject to the terms of the Mozilla Public
#  * License, v. 2.0. If a copy of the MPL was not distributed with this
#  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
#  */
#

#! /bin/bash

echo "Running post installation script"
chmod 777 /opt/${appSafeName}
ln -s /opt/${appSafeName}/${appSafeName}.desktop /usr/share/applications/${appSafeName}.desktop
