# This looks for unused styles in GWT code. 
# Gets a list of images, and looks to find if they are used. 
#
# Author: Michael Neale
#

#directory recursive processor...
def mapdir(dir, predicate, action) 
    Dir.foreach(dir) do |d| 
      if d != "." && d != ".." then
        sub = "#{dir}/#{d}"        
        if not File.directory? sub and predicate.call(sub) then
          action.call(sub)
        else 
          if File.directory? sub and not sub.include? ".svn" 
	  then mapdir(sub, predicate, action) end
        end        
      end
    end
end


def process(css, sources_root) 
  style_names = []
  IO.foreach(css) do |line|
    #rip the name from the line if is starts with "."
    if line.slice(0, 1) == "." then
      puts "Line is: " + line
      style_names << line.slice(1, line.index(' ') - 1)
    end
  end

  mapdir sources_root, lambda {  |f| f.include? ".java" or f.include? ".html" or f.include? ".js" }, 
    lambda do |f|
      contents = IO.read(f)
      style_names.each do |style|
       if contents.include? '"' + style + '"' then
         style_names = style_names - [style]
       end
      end
    end

  puts "#{style_names.size} unused styles : "
  puts style_names
end

process "/Users/michaelneale/project/jboss-rules/drools-guvnor/src/main/java/org/drools/brms/public/JBRMS.css", "/Users/michaelneale/project/jboss-rules/drools-guvnor/src/main/java/org/drools/brms"
