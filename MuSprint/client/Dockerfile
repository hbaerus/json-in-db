# LICENSE UPL 1.0
#
# Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
#

###############################################################################
# Dockerfile for MuSprint Stories frontend (React) server
###############################################################################

ARG        BASE_IMAGE=oraclelinux:9
FROM       ${BASE_IMAGE}

LABEL      "provider"="Oracle"
LABEL      "maintainer"="Srikrishnan Suresh <srikrishnan.s.suresh@oracle.com>"
LABEL      "version"=1.0.0

WORKDIR    /app
COPY       . /app

#
# Node.js module
#
RUN        dnf install -y nodejs npm && \
           dnf clean all
RUN        node -v
RUN        npm -v

#
# Dependent node.js modules
#
RUN        npm install
RUN        useradd -r app && chown -R app /app
USER       app

#
# Prepare runnable
#
EXPOSE     3000
ENTRYPOINT [ "npm", "start" ]
