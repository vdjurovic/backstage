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
        <title>Applications</title>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-Zenh87qX5JnK2Jl0vWa8Ck2rdkQ2Bzep5IDxbcnCeuOxjzrPF/et3URy9Bv1WTRi" crossorigin="anonymous">
    </head>
    <body>
        <div class="container">
            <header class="d-flex flex-wrap justify-content-center py-3 mb-4 border-bottom">
                <a href="/" class="d-flex align-items-center mb-3 mb-md-0 me-md-auto text-dark text-decoration-none">
                    <span class="fs-4">Applications</span>
                </a>
            </header>
                <#list appList as application>
                    <#if (application?counter % 4) == 1>
                        <div class="row mt-2" >
                    </#if>
                    <div class="col">
                        <div class="card" style="width: 20rem;margin-bottom: 3rem;">
                            <img src="/default-app-icon.svg" class="card-img-top" alt="${application.name}"  />
                            <div class="card-body">
                                <h5 class="card-title">${application.name}</h5>
                                <p class="card-text">${application.headline}</p>
                                <a href="/ui/application/${application.id}" class="btn btn-primary">Details</a>
                            </div>
                        </div>
                    </div>
                    <#if (application?counter % 4) == 0>
                    </div>
                    </#if>
                </#list>
        </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.2/dist/js/bootstrap.bundle.min.js" integrity="sha384-OERcA2EqjJCMA+/3y+gxIOqMEjwtxJY7qPCqsdltbNJuaOe923+mo//f6V8Qbsw3" crossorigin="anonymous"></script>
    </body>
</html>
