# File: java_project_dump.py
import os
import sys
import argparse
from pathlib import Path

# --- Configuration pour un projet Java Maven ---
IGNORE_DIRS = {
    'target',  # Dossier des builds Maven
    '.git', '.idea', '.vscode', '__pycache__'
}
IGNORE_FILES = {
    '.DS_Store',
    'java_dump.py',  # Ignore le script lui-même
    'java_snapshot.txt'     # Ignore le fichier de sortie
}
# Ignore les fichiers compilés et autres binaires
IGNORE_EXTENSIONS = {
    '.class', '.jar', '.log',
    '.ico', '.png', '.jpg', '.jpeg', '.gif', '.svg', '.webp',
    '.woff', '.woff2', '.ttf', '.eot',
    '.pdf', '.zip', '.map',
}
MAX_FILE_SIZE = 2 * 1024 * 1024  # 2 Mo

def get_language_from_extension(filename):
    """Retourne le nom du langage pour la coloration syntaxique Markdown."""
    extension_map = {
        '.java': 'java',
        '.xml': 'xml',         # Pour pom.xml
        '.md': 'markdown',
        '.json': 'json',
        '.properties': 'properties',
        '.sh': 'shell',
        '.bat': 'batch',       # Pour mvnw.cmd
        '.yml': 'yaml',
        '.yaml': 'yaml',
        '.py': 'python'
    }
    suffix = Path(filename).suffix
    return extension_map.get(suffix, '')

def generate_project_structure(root_path):
    """Génère une chaîne de caractères représentant l'arborescence du projet."""
    tree = []
    files_to_include = []

    for dirpath, dirnames, filenames in os.walk(root_path, topdown=True):
        # Filtre les répertoires à ignorer
        dirnames[:] = [d for d in dirnames if d not in IGNORE_DIRS]
        
        rel_path = os.path.relpath(dirpath, root_path)
        level = rel_path.count(os.sep) if rel_path != "." else 0
        
        indent = '    ' * level
        if rel_path != ".":
            tree.append(f"{indent}|-- {os.path.basename(dirpath)}/")

        sub_indent = '    ' * (level + 1)
        for filename in sorted(filenames):
            if (filename in IGNORE_FILES or 
                any(filename.endswith(ext) for ext in IGNORE_EXTENSIONS)):
                continue
            
            file_path = os.path.join(dirpath, filename)
            files_to_include.append(file_path)
            tree.append(f"{sub_indent}|-- {filename}")
            
    return "\n".join(tree), files_to_include


def get_file_contents(files_to_include, root_path):
    """Lit le contenu des fichiers et le formate pour la sortie."""
    contents = []
    
    for file_path in files_to_include:
        relative_path = os.path.relpath(file_path, root_path)
        
        try:
            if os.path.getsize(file_path) > MAX_FILE_SIZE:
                contents.append(f"--- PATH: {relative_path} ---\n")
                contents.append(f"```\n# File content ignored because its size exceeds {MAX_FILE_SIZE / 1024 / 1024:.0f} MB.\n```\n")
                continue
        except OSError:
            continue
            
        try:
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as f:
                content = f.read()
            lang = get_language_from_extension(os.path.basename(file_path))
            contents.append(f"--- PATH: {relative_path} ---\n")
            contents.append(f"```{lang}\n{content.strip()}\n```\n")
        except Exception as e:
            contents.append(f"--- PATH: {relative_path} ---\n")
            contents.append(f"```\n# Could not read file: {e}\n```\n")
            
    return "\n".join(contents)


def main():
    """Fonction principale du script."""
    parser = argparse.ArgumentParser(
        description="Generates a comprehensive snapshot from the structure and content of a Java Maven project.",
        formatter_class=argparse.RawTextHelpFormatter
    )
    parser.add_argument(
        "project_path",
        help="The path to the root directory of the Java project."
    )
    parser.add_argument(
        "-o", "--output",
        help="The name of the file to save the result to. If not specified, prints to the console."
    )
    args = parser.parse_args()
    
    project_path = args.project_path
    output_file = args.output
    
    if not os.path.isdir(project_path):
        print(f"❌ Error: The path '{project_path}' is not a valid directory.", file=sys.stderr)
        sys.exit(1)
        
    print(f"Analyzing Java Maven project at: {os.path.abspath(project_path)}\n")
    
    tree_structure, files_to_include = generate_project_structure(project_path)
    file_contents = get_file_contents(files_to_include, project_path)
    
    final_output = (
        f"This is the structure and content of a Java Maven project. My goal is to [...Describe your objective here...]. Please analyze the code for inconsistencies, suggest improvements, and complete any missing parts.\n\n"
        f"## Project Structure\n\n"
        f"```\n{tree_structure}\n```\n\n"
        f"## File Contents\n\n"
        f"{file_contents}"
    )
    
    if output_file:
        try:
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(final_output)
            print(f"✅ Success! The project dump has been saved to the file: {output_file}")
        except IOError as e:
            print(f"❌ Error writing to file '{output_file}': {e}", file=sys.stderr)
            sys.exit(1)
    else:
        print(final_output)

if __name__ == "__main__":
    main()