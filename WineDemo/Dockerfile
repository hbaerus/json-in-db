FROM oraclelinux:9

COPY . /app

RUN dnf install -y oracle-release-el9
RUN dnf install -y oracle-instantclient19.19-basic.x86_64
RUN dnf install -y nodejs npm
RUN dnf install -y bzip2

RUN npm -g install @oracle/ojet-cli
RUN npm -g install pm2

RUN cd /app && npm install && ojet build
RUN useradd -r app && chown -R app /app
USER app

CMD ["pm2-runtime", "/app/process.json"]
