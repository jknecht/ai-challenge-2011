jar cfm mybot.jar MANIFEST -C bin/ .

python tools/playgame.py "java -jar mybot.jar" "java -jar mybot9.jar" --map_file tools/maps/maze/maze_02p_01.map --log_dir game_logs --turns 600 --verbose -e

