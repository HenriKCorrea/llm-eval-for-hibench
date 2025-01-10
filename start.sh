docker run \
    --name litellm \
    -v $(pwd)/litellm_config.yaml:/app/config.yaml \
    -e GOOGLE_AI_API_KEY="$(echo $GOOGLE_AI_API_KEY)" \
    -p 4000:4000 \
    ghcr.io/berriai/litellm:main-v1.57.1 \
    --config /app/config.yaml --detailed_debug \