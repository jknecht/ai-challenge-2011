jar cfm mybot.jar MANIFEST -C bin/ .

python tools/playgame.py "java -jar mybot.jar" "python tools/sample_bots/python/HunterBot.py" --map_file tools/maps/maze/maze_02p_01.map --log_dir game_logs --turns 600 --verbose -e

