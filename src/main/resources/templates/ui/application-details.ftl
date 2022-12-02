<!--
  ~ /*
  ~  * Copyright (c) 2022  Bitshift D.O.O (http://bitshifted.co)
  ~  *
  ~  * This Source Code Form is subject to the terms of the Mozilla Public
  ~  * License, v. 2.0. If a copy of the MPL was not distributed with this
  ~  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  ~  */
  -->
<!DOCTYPE html>
<html>
    <head>
        <title>${application.name} - Backstage</title>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi" crossorigin="anonymous">
    </head>
    <body>
        <div class="container">
            <header class="d-flex flex-wrap justify-content-center py-3 mb-4 border-bottom">
                <a href="/" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-dark text-decoration-none">
                    <!--                    <svg class="bi me-2" width="40" height="32"><use xlink:href="#bootstrap"/></svg>-->
                    <span class="fs-4">${application.name}</span>
                </a>
            </header>
            <div class="row mb-3">
                <div class="col-3">
                <a  class="btn btn-primary" href="/ui/applications">
                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-arrow-return-left" viewBox="0 0 16 16">
                        <path fill-rule="evenodd" d="M14.5 1.5a.5.5 0 0 1 .5.5v4.8a2.5 2.5 0 0 1-2.5 2.5H2.707l3.347 3.346a.5.5 0 0 1-.708.708l-4.2-4.2a.5.5 0 0 1 0-.708l4-4a.5.5 0 1 1 .708.708L2.707 8.3H12.5A1.5 1.5 0 0 0 14 6.8V2a.5.5 0 0 1 .5-.5z"/>
                    </svg>
                    Back to applications list
                </a>
            </div>
            </div>
            <div class="row">
                <div class="col-lg-3 col-md-3 col-sm-4">
                    <img src="/default-app-icon.svg" class="img-thumbnail" alt="${application.name}"  />
                </div>
                <div class="col-lg-9 col-md-9 col-sm-8">
                    <h4 class="box-title mt-5">${application.name}</h4>
                    <p>${application.headline}</p>
                    <h2 class="mt-5">
                        Download installer for your operating system
                    </h2>
                    <#list installers as installer >
                    <a class="btn btn-primary btn-rounded" href="/v1/applications/${installer.applicationId}/installers/${installer.fileHash}">${installer.operatingSystem?lower_case?capitalize} ${installer.cpuArch?lower_case} (.${installer.extension})</a>
                    </#list>

                    <h3 class="box-title mt-5">Description</h3>
                    <div>
                        ${application.description}
                    </div>
                </div>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3" crossorigin="anonymous"></script>
</body>
        </html>
