#
# This looks for unused images in GWT code. 
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


def process(image_root, sources_root)
  images = []
  mapdir image_root, lambda { |f| true }, lambda { |f| images << f }

  mapdir sources_root, lambda { |f| f.include? ".java" or f.include? ".html" or f.include? ".js" }, 
   lambda do |f|
    contents = IO.read(f)
    images.each do |img|
      if contents.include? img.split("/images/")[1] then
         images = images - [img]
      end
    end 
   end
         
  puts "#{images.size} Unused images: \n " 
  puts images


end

process "/Users/michaelneale/project/jboss-rules/drools-guvnor/src/main/java/org/drools/brms/public/images", "/Users/michaelneale/project/jboss-rules/drools-guvnor/src/main/java/org/drools/brms"
