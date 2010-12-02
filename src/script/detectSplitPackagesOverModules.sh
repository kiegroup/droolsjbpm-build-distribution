#! /bin/sh
# @author Geoffrey De Smet

ls -R | grep "src/main/java/" | sed "s/\(.*\/src\/main\/java\/\)\(.*\)\:/\2/g" | sort | uniq -c | grep -v "\s*1 "
