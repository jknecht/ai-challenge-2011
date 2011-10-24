jar cfm mybot.jar MANIFEST -C bin/ .

python tools/playgame.py "java -jar mybot.jar" "java -jar mybot.jar" "java -jar mybot.jar" "java -jar mybot.jar" "java -jar mybot.jar" --map_file tools/maps/maze/maze_05p_01.map --log_dir game_logs --turns 500 --verbose -e

