import sys 

def parse_path(path):
    parts = path.split('/')
    return parts[-2], parts[-1]

if len(sys.argv) != 2:
    print("Select path to result dir")
    sys.exit(1)

f = open(f"{sys.argv[1]}/clonepairs.txt")
data = f.readlines()
f.close()
f = open(f"{sys.argv[1]}/fix_clonepairs.csv", "w")
for line in data:
    path1, start1, end1, path2, start2, end2 = line.split(",")
    dir1, file1 = parse_path(path1)
    dir2, file2 = parse_path(path2)
    f.write(','.join([dir1, file1, start1, end1, dir2, file2, start2, end2]))
f.close()
