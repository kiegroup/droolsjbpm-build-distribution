#!/usr/bin/python

#
# This script will flatten out a folder based docbook manual into a docbot friendly "flat" structure
# (and update links in files accordingly)
# Author: Michael Neale
#

import os, sys, shutil

def flatten(root, output) :  
    if not os.path.isdir(output):
        os.mkdir(output)
    if not os.path.isdir(os.path.join(output, "images")):
        os.mkdir(os.path.join(output, "images"))
    sections = {}   
    top_files = []
    names = os.listdir(root)
    for name in names:
        if os.path.isdir(os.path.join(root, name)) :
            if not name == ".svn":
                flattenDir(root, name, output, sections)
        else:
            if name.endswith(".xml") :
                top_files.append(name)
            elif name != ".svn":
                shutil.copyfile(os.path.join(root, name), os.path.join(output, name))
    for file in top_files:        
        contents = open(os.path.join(root, file), "r").read()
        for section in sections:
            contents = contents.replace(section, sections[section])
        outfile = open(os.path.join(output, file), "w")
        outfile.write(contents)
        
                    
            
            


def flattenDir(root, dir, output, sections):
    docs = []
    images = []
    names = os.listdir(os.path.join(root, dir))
    for name in names:
        if name.endswith(".xml"):
            docs.append(name)
        else:
            if name != ".svn":
                images.append(name)
                shutil.copyfile(os.path.join(root, dir, name), os.path.join(output, "images", dir + "_" + name))
    for doc in docs: 
        new_name = dir + "_" + doc
        sections[dir + "/" + doc] = new_name
        file = open(os.path.join(root, dir, doc), "r").read()
        outfile = open(os.path.join(output, new_name), "w")
        for img in images:
            file = file.replace(img, "images/" + dir + "_" + img)
        outfile.write(file)

        


if len(sys.argv) < 2:
    print "2 arguments required: <path to root of documentation> <output path>. eg: docbot-masseur.py ./something ./output"
else:
    flatten(sys.argv[1], sys.argv[2])
    
