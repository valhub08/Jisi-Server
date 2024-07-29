FROM ubuntu:latest
LABEL authors="kmg"

ENTRYPOINT ["top", "-b"]