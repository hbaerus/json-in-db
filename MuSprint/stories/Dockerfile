# LICENSE UPL 1.0
#
# Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
#

###############################################################################
# Dockerfile for MuSprint Stories service
###############################################################################

ARG        BASE_IMAGE=oraclelinux:9
FROM       ${BASE_IMAGE}

LABEL      "provider"="Oracle"
LABEL      "maintainer"="Srikrishnan Suresh <srikrishnan.s.suresh@oracle.com>"
LABEL      "version"=1.0.0

WORKDIR    /app
COPY       . /app

#
# Oracle Instant Client and SQL*Plus Client
#
ARG        RELEASE=19
ARG        UPDATE=19

RUN        dnf -y install oracle-release-el9 && \
           dnf -y install oracle-instantclient${RELEASE}.${UPDATE}-basic && \
           dnf -y install oracle-instantclient${RELEASE}.${UPDATE}-sqlplus && \
           dnf clean all
RUN        sqlplus -v

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
EXPOSE     5000
ENTRYPOINT [ "npm", "start" ]
