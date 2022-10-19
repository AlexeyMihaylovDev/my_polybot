FROM ubuntu as installer
RUN apt-get update \
  && apt-get install -y curl unzip

RUN curl https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip -o awscliv2.zip \
  && unzip awscliv2.zip \
  && ./aws/install --bin-dir /aws-cli-bin/ \
  && rm -rf aws awscliv2.zip 

RUN apt-get update && apt-get install -y --no-install-recommends \
    python3.5 \
    python3-pip \
    && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN pip3 install nibabel pydicom matplotlib pillow
RUN pip3 install med2image

RUN mkdir /snyk && cd /snyk \
    && curl https://static.snyk.io/cli/v1.666.0/snyk-linux -o snyk \
    && chmod +x ./snyk
RUN \
# Update
apt-get update -y && \
# Install Unzip
apt-get install unzip -y && \
# need wget
apt-get install wget -y && \
# vim
apt-get install vim -y
# Download terraform for linux
RUN wget https://releases.hashicorp.com/terraform/1.3.1/terraform_1.3.1_linux_amd64.zip
# Unzip
RUN unzip terraform_1.3.1_linux_amd64.zip

# Maven
# Setting Maven Version that needs to be installed
ARG MAVEN_VERSION=3.8.6
RUN curl -fsSL https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz | tar xzf - -C /usr/share \
  && mv /usr/share/apache-maven-$MAVEN_VERSION /usr/share/maven \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

RUN wget https://github.com/allure-framework/allure2/releases/download/2.19.0/allure-2.19.0.tgz
RUN tar -xvf allure-2.19.0.tgz


RUN curl -s -L https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-4.7.0.2747.zip -o sonarscanner.zip \
  && unzip -qq sonarscanner.zip \
  && rm -rf sonarscanner.zip \
  && mv sonar-scanner-4.7.0.2747 sonar-scanner
COPY  sonar-scanner/conf/sonar-scanner.properties  sonar-scanner/conf/sonar-scanner.properties








FROM jenkins/agent
COPY --from=docker /usr/local/bin/docker /usr/local/bin/
COPY --from=installer /usr/local/aws-cli/ /usr/local/aws-cli/
COPY --from=installer /aws-cli-bin/ /usr/local/bin/
COPY --from=installer /snyk/ /usr/local/bin/
COPY --from=installer terraform /usr/local/bin/
COPY --from=installer /usr/share/maven /usr/share/maven
COPY --from=installer /usr/share/maven/bin/mvn /usr/bin/mvn
COPY --from=installer allure-2.19.0/ /usr/local/allure/
COPY --from=installer allure-2.19.0/bin/ /usr/local/bin/
COPY --from=installer sonar-scanner /usr/local/sonar-scanner/
COPY --from=installer sonar-scanner/bin/ /usr/local/bin/
COPY --from=installer sonar-scanner/conf/sonar-scanner.properties /usr/local/sonar-scanner/conf/sonar-scanner.properties

ENV SONAR_RUNNER_HOME=/usr/local/sonar-scanner/
ENV SONAR $PATH:$SONAR_RUNNER_HOME/bin
ENV PATH $SONAR_RUNNER_HOME:$PATH

ENV ALLURE_HOME  /usr/local/allure
ENV ALLURE $ALLURE_HOME/bin
ENV PATH $ALLURE:$PATH

ENV MAVEN_VERSION=${MAVEN_VERSION}
ENV M2_HOME /usr/share/maven
ENV maven.home $M2_HOME
ENV M2 $M2_HOME/bin
ENV PATH $M2:$PATH