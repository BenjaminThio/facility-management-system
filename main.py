import os

# --- Configuration ---
# Set the folder you want to scan here (e.g., 'src', '.', 'lib')
TARGET_DIRECTORY = 'src' 

# Folders to completely ignore
IGNORE_FOLDERS = {'.git', '.idea', '.vscode', 'bin', 'out', 'target', 'node_modules', 'database', 'sounds'}

# Specific file names to ignore
IGNORE_FILES = {'.gitignore', '.DS_Store', 'facility-management-system.txt', 'project_content_summary.txt'}

# Only process these extensions (Add or remove as needed)
ALLOWED_EXTENSIONS = {'.java', '.txt', '.xml', '.json', '.md'}

OUTPUT_FILE = 'project_content_summary.txt'
# ---------------------

def should_process(file_name, root):
    # Check if any parent folder is in the ignore list
    parts = os.path.normpath(root).split(os.sep)
    if any(ignored in parts for ignored in IGNORE_FOLDERS):
        return False
    
    # Check file name and extension
    if file_name in IGNORE_FILES:
        return False
    
    ext = os.path.splitext(file_name)[1].lower()
    return ext in ALLOWED_EXTENSIONS

def generate_project_summary(target_dir):
    # Check if the target directory exists
    if not os.path.exists(target_dir):
        print(f"Error: The directory '{target_dir}' does not exist.")
        return

    current_line = 1
    
    with open(OUTPUT_FILE, 'w', encoding='utf-8') as outfile:
        # os.walk is naturally recursive, it will go through all nested folders
        for root, dirs, files in os.walk(target_dir):
            # Sort directories and files for consistent, alphabetical output
            dirs.sort()
            files.sort()
            
            for file in files:
                if should_process(file, root):
                    file_path = os.path.join(root, file)
                    
                    # Get relative path for the header and ensure forward slashes
                    rel_path = os.path.relpath(file_path, '.')
                    rel_path = rel_path.replace(os.sep, '/')
                    
                    try:
                        with open(file_path, 'r', encoding='utf-8') as infile:
                            content = infile.readlines()
                            
                            # Write the header with current line count
                            outfile.write(f"{rel_path}\n")
                            outfile.write("```\n")
                            
                            # Write file content line by line
                            for line in content:
                                outfile.write(line)
                                current_line += 1
                                
                            # Ensure there's a newline at the end of the file content
                            if content and not content[-1].endswith('\n'):
                                outfile.write('\n')
                                
                            outfile.write("```\n\n")
                            
                            # Account for the header and closing backticks
                            current_line += 3 
                            
                    except Exception as e:
                        print(f"Could not read {rel_path}: {e}")

    print(f"Successfully generated: {OUTPUT_FILE} from the '{target_dir}' directory.")

if __name__ == "__main__":
    generate_project_summary(TARGET_DIRECTORY)