FROM ubuntu as installer
RUN apt-get update \
  && apt-get install -y curl unzip

RUN curl https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip -o awscliv2.zip \
  && unzip awscliv2.zip \
  && ./aws/install --bin-dir /aws-cli-bin/ \
  && rm -rf aws awscliv2.zip 
RUN apt-get install docker-engine -y
RUN service docker start

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

FROM jenkins/agent
COPY --from=docker /usr/local/bin/docker /usr/local/bin/
COPY --from=installer /usr/local/aws-cli/ /usr/local/aws-cli/
COPY --from=installer /aws-cli-bin/ /usr/local/bin/
COPY --from=installer /snyk/ /usr/local/bin/
