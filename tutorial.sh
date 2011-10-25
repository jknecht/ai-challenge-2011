jar cfm mybot.jar MANIFEST -C bin/ .

python tools/playgame.py "java -jar mybot.jar" "java -jar mybot10.jar" --map_file tools/maps/example/tutorial1.map --log_dir game_logs --turns 200 --verbose -e

