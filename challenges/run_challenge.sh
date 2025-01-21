#!/bin/bash

# Retrieve script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Default values
task_name=""
framework=""
llm=""

# Function to display usage
usage() {
    echo "Usage: $0 [-t task_name] [-f framework] [-l llm]"
    echo "  -t task_name   : Challenge to be executed."
    echo "                   Available options: 'wordcount'."
    echo "  -f framework   : Framework that will run the application."
    echo "                   Can be either 'spark' or 'flink'."
    echo "  -l llm         : Implementation to be used to solve the challenge following its LLM creator name"
    echo "                   Can be either 'gemini', 'llama' or 'phi'."
    echo "  -h             : Display this help message."
    exit 1
}

# Parse arguments
while getopts "t:f:l:h:" opt; do
    case ${opt} in
        t )
            task_name=$OPTARG
            ;;
        f )
            framework=$OPTARG
            ;;
        l )
            llm=$OPTARG
            ;;
        h )
            usage
            ;;
        * )
            echo "Invalid option: $opt"
            usage
            ;;
    esac
done

# Validate task_name argument
if [[ "$task_name" != "wordcount" ]]; then
    echo "Error: invalid task name '$task_name'."
    usage
fi

# Validate framework argument
if [[ "$framework" != "spark" && "$framework" != "flink" ]]; then
    echo "Error: invalid framework '$framework'."
    usage
fi

# Validate llm argument
if [[ "$llm" != "gemini" && "$llm" != "llama" && "$llm" != "phi" ]]; then
    echo "Error: invalid llm '$llm'."
    usage
fi

# Set container name
container_name="$framework-$task_name-$llm"

# Set container framework home path
framework_home=""
[ "$framework" == "spark" ] && framework_home="/opt/spark" 

# Set container work directory
container_workdir=""
[ "$framework" == "spark" ] && container_workdir="$framework_home/work-dir"

# Set container input directory
container_input_dir="$container_workdir/input"

# Set container output directory
container_output_dir="$container_workdir"

# Set Container binaries directory
container_bin_dir="$framework_home/bin"

# Set host input directory
host_input_dir="$SCRIPT_DIR/input"

# Set host output directory
host_output_dir="$SCRIPT_DIR/output"

# Set host binaries directory
host_bin_dir="$SCRIPT_DIR/$task_name/$framework/target/scala-2.12"

# Set container image
container_image=""
[ "$framework" == "spark" ] && container_image="apache/spark:3.5.4-scala2.12-java11-python3-r-ubuntu"

# Set container entrypoint
container_entrypoint=""
[ "$framework" == "spark" ] && container_entrypoint="$container_bin_dir/spark-submit"

# Set java class name to run
java_class_name=""
[ "$container_name" == "spark-wordcount-gemini" ] && java_class_name="WordCountGeminiApp"
[ "$container_name" == "spark-wordcount-phi" ] && java_class_name="WordCountPhiApp"
[ "$container_name" == "spark-wordcount-llama" ] && java_class_name="WordCountLlamaApp"

# Set JAR name
jar_name=""
[ "$framework" == "spark" ] && [ "$task_name" == "wordcount" ] && jar_name="spark-wordcount-app_2.12-1.0.jar"
[ "$framework" == "flink" ] && [ "$task_name" == "wordcount" ] && jar_name="flink-wordcount-app_2.12-1.0.jar"

# Set Application bin directory
application_bin_dir="$container_workdir/bin"

# Set Application jar
application_jar="$application_bin_dir/$jar_name"

# Set Application input
application_input_list=()
[ "$task_name" == "wordcount" ] && application_input+=("$container_input_dir/dracula.txt")

# Set Application output
application_output="$container_output_dir/$container_name"

build_jar() {
    # Skip if jar already exists
    if [ -f "$host_bin_dir/$jar_name" ]; then
        return
    fi

    # Build the jar
    echo "Building application"
    pushd "$SCRIPT_DIR/$task_name/$framework" > /dev/null
    if ! sbt package; then
        echo "Error: unable to build jar."
        popd > /dev/null
        exit 1
    fi
    popd > /dev/null
}

remove_container() {
    # Test if container is running
    if [ "$(docker ps -aqf name=$container_name)" ]; then
        # Remove container
        printf "%s " "Removing container"
        if ! docker rm "$container_name"; then
            echo "Error: unable to remove container."
            exit 1
        fi
    fi
}

create_output_directory() {
    if ! mkdir -p "$host_output_dir"; then
        echo "Error: unable to create output directory."
        exit 1
    fi
}

copy_results() {
    if ! docker cp "$container_name:$application_output" "$host_output_dir"; then
        echo "Error: unable to copy results."
        exit 1
    fi
}

# Run the task on the selected framework using the selected LLM implementation in docker container
execute_task() {
    echo "Executing task '$task_name' using framework '$framework' and LLM '$llm'..."

    # Create docker run arguments
    local -a docker_run_cmd=("docker" "run")
    docker_run_cmd+=("--name" "$container_name")
    docker_run_cmd+=("-v" "$host_input_dir:$container_input_dir")
    docker_run_cmd+=("-v" "$host_bin_dir:$application_bin_dir")
    docker_run_cmd+=("$container_image")
    docker_run_cmd+=("$container_entrypoint")
    docker_run_cmd+=("--class" "$java_class_name")
    docker_run_cmd+=("--master" "local[4]")
    docker_run_cmd+=("$application_jar")
    docker_run_cmd+=("$application_input")
    docker_run_cmd+=("$application_output")

    # Run the task
    if ! "${docker_run_cmd[@]}"; then
        echo "Error: unable to execute task."
        exit 1
    fi
}

# Main function
build_jar
remove_container
create_output_directory
execute_task
copy_results
remove_container

# docker stop flink; docker rm flink; docker run --detach --name flink -p 8081:8081 flink:1.13.0-scala_2.12-java11 /docker-entrypoint.sh jobmanager && docker exec --detach flink /docker-entrypoint.sh taskmanager && docker exec --user flink flink mkdir -p /opt/flink/work-dir/input/ && docker cp /home/henrique/repo/llm-eval-for-hibench/challenges/input/dracula.txt flink:/opt/flink/work-dir/input/ && docker cp /home/henrique/repo/llm-eval-for-hibench/challenges/wordcount/flink/target/scala-2.12/flink-wordcount-app_2.12-1.0.jar flink:/opt/flink/work-dir && docker exec flink flink run /opt/flink/work-dir/flink-wordcount-app_2.12-1.0.jar --input /opt/flink/work-dir/input/dracula.txt --output /opt/flink/work-dir/AppOutput.txt && docker cp flink:/opt/flink/work-dir/AppOutput.txt /home/henrique/repo/llm-eval-for-hibench/challenges/output/ && docker stop flink && docker rm flink
