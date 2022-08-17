FROM gradle:6-jdk8

COPY . /guideforce
WORKDIR /guideforce
RUN gradle build

CMD gradle --warning-mode none run