jar cfm mybot.jar MANIFEST -C bin/ .

python tools/playgame.py "java -jar mybot.jar" "java -jar mybot9.jar" "java -jar mybot9.jar" "java -jar mybot9.jar" "java -jar mybot9.jar" --map_file tools/maps/maze/maze_05p_01.map --log_dir game_logs --turns 1000 --verbose -e

