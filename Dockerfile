FROM openjdk:19-jdk-alpine
USER root
COPY build/libs/cma-app-1.0.jar .
ADD dependency /dependency
ARG CONFIG_NAME=application
ENV CONFIG_NAME=${CONFIG_NAME}
RUN /bin/sh
RUN chmod +x /dependency/keepassxc/usr/bin/keepassxc-cli
RUN echo '#!/bin/bash\n/dependency/keepassxc/usr/bin/keepassxc-cli "$@"' > /usr/bin/keepassxc-cli && chmod +x /usr/bin/keepassxc-cli
ENTRYPOINT java -jar -Dspring.config.location=classpath:/config/$CONFIG_NAME.properties cma-app-1.0.jar