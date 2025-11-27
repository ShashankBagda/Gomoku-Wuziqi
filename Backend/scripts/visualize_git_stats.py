#!/usr/bin/env python3
"""
Parse git log numstat output and generate visualization charts
"""

import re
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from collections import defaultdict
import numpy as np

def parse_mailmap(mailmap_path):
    """Parse .mailmap file to get author mappings"""
    # Format: canonical_name <canonical_email> original_name <original_email>
    mailmap = {}
    try:
        with open(mailmap_path, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                # Parse: canonical_name <canonical_email> [original_name] <original_email>
                parts = re.findall(r'([^<]+)<([^>]+)>', line)
                if len(parts) >= 2:
                    canonical_name = parts[0][0].strip()
                    canonical_email = parts[0][1].strip()
                    original_email = parts[1][1].strip()
                    mailmap[original_email.lower()] = (canonical_name, canonical_email)
    except FileNotFoundError:
        pass
    return mailmap

# Load mailmap
MAILMAP = parse_mailmap(r'D:\NUS\gomoku-all\gomuku-backend\.mailmap')

def normalize_author(name, email):
    """Normalize author names using .mailmap"""
    email_lower = email.lower()

    # Check mailmap first
    if email_lower in MAILMAP:
        return MAILMAP[email_lower][0]

    # Fallback to manual mapping for unmapped emails
    if 'e1538880@u.nus.edu' in email_lower or 'haotian@codemao.cn' in email_lower:
        return 'haotian'
    elif 'e1582337@u.nus.edu' in email_lower:
        return 'chengmuqin'
    elif 'e1660978@u.nus.edu' in email_lower or 'shashankbagda167@gmail.com' in email_lower:
        return 'shashank'
    elif 'e1554460@u.nus.edu' in email_lower or '1962249400@qq.com' in email_lower:
        return 'liyuanxing'
    elif 'zpy' in name.lower() or 'e1493782@u.nus.edu' in email_lower:
        return 'zpy'
    elif 'txy' in name.lower():
        return 'txy'
    elif 'yinxiaofeng' in name.lower():
        return 'yinxiaofeng'
    else:
        return name

def get_module_from_path(filepath):
    """
    Determine which module a file belongs to
    Modules: gateway, matching(game lobby), game, game room, user, ranking, dingzuhao-learning
    """
    if not filepath or filepath == '-':
        return 'config/root'

    # Gateway
    if filepath.startswith('gateway/'):
        return 'gateway'

    # Game Lobby (matching)
    if filepath.startswith('gomoku/gomoku-matching/') or filepath.startswith('matching/'):
        return 'matching (game lobby)'

    # Game Room
    if filepath.startswith('gomoku/gomoku-room/') or filepath.startswith('room/'):
        return 'game room'

    # Game (gomoku game logic - excluding matching and room)
    if filepath.startswith('gomoku/'):
        return 'game'

    # User
    if filepath.startswith('user/'):
        return 'user'

    # Ranking
    if filepath.startswith('ranking/'):
        return 'ranking'

    # Common/shared libraries
    if filepath.startswith('common/'):
        return 'common/shared'

    # Dingzuhao learning (if exists)
    if 'dingzuhao' in filepath.lower() or 'learning' in filepath.lower():
        return 'dingzuhao-learning'

    # Root-level files (pom.xml, .gitlab-ci.yml, docker files, etc.)
    return 'config/root'

def parse_git_log(filename):
    """Parse the git log numstat file"""
    commits = []
    current_commit = None
    DELIMITER = chr(31)  # ASCII Unit Separator

    with open(filename, 'r', encoding='utf-8', errors='ignore') as f:
        for line in f:
            line = line.rstrip('\n')

            if not line:
                continue

            # Commit header line
            if DELIMITER in line:
                parts = line.split(DELIMITER)
                if len(parts) >= 5:
                    if current_commit:
                        commits.append(current_commit)

                    current_commit = {
                        'hash': parts[0],
                        'author': parts[1],
                        'email': parts[2],
                        'date': parts[3],
                        'message': DELIMITER.join(parts[4:]),
                        'files': []
                    }

            # File stat line
            elif '\t' in line and current_commit:
                parts = line.split('\t')
                if len(parts) >= 3:
                    try:
                        added = 0 if parts[0].strip() == '-' else int(parts[0].strip())
                        deleted = 0 if parts[1].strip() == '-' else int(parts[1].strip())
                        filepath = parts[2].strip()
                        current_commit['files'].append({
                            'added': added,
                            'deleted': deleted,
                            'path': filepath
                        })
                    except (ValueError, IndexError):
                        pass

    if current_commit:
        commits.append(current_commit)

    return commits

def analyze_contributions(commits):
    """Analyze contributions by author and module"""
    author_stats = defaultdict(lambda: {
        'commits': 0,
        'modules': defaultdict(lambda: {'added': 0, 'deleted': 0})
    })

    module_stats = defaultdict(lambda: {'added': 0, 'deleted': 0})

    for commit in commits:
        author = normalize_author(commit['author'], commit['email'])
        author_stats[author]['commits'] += 1

        for file_stat in commit['files']:
            module = get_module_from_path(file_stat['path'])

            # Update author stats
            author_stats[author]['modules'][module]['added'] += file_stat['added']
            author_stats[author]['modules'][module]['deleted'] += file_stat['deleted']

            # Update module stats
            module_stats[module]['added'] += file_stat['added']
            module_stats[module]['deleted'] += file_stat['deleted']

    return dict(author_stats), dict(module_stats)

def create_visualizations(author_stats, module_stats):
    """Create comprehensive visualization charts"""

    # Set style
    plt.style.use('default')
    colors = plt.cm.Set3(np.linspace(0, 1, 12))

    # Create figure with subplots
    fig = plt.figure(figsize=(20, 12))

    # 1. Module Distribution Pie Chart
    ax1 = plt.subplot(2, 3, 1)
    module_names = list(module_stats.keys())
    module_lines = [stats['added'] for stats in module_stats.values()]

    wedges, texts, autotexts = ax1.pie(module_lines, labels=module_names, autopct='%1.1f%%',
                                         colors=colors[:len(module_names)], startangle=90)
    ax1.set_title('Code Distribution by Module\n(Lines Added)', fontsize=14, fontweight='bold')

    # Make percentage text readable
    for autotext in autotexts:
        autotext.set_color('black')
        autotext.set_fontweight('bold')
        autotext.set_fontsize(9)

    # 2. Contributor Commits Bar Chart
    ax2 = plt.subplot(2, 3, 2)
    authors = list(author_stats.keys())
    commits = [stats['commits'] for stats in author_stats.values()]

    bars = ax2.barh(authors, commits, color=colors[:len(authors)])
    ax2.set_xlabel('Number of Commits', fontsize=12)
    ax2.set_title('Commits per Contributor', fontsize=14, fontweight='bold')
    ax2.grid(axis='x', alpha=0.3)

    # Add value labels
    for i, bar in enumerate(bars):
        width = bar.get_width()
        ax2.text(width, bar.get_y() + bar.get_height()/2, f' {int(width)}',
                ha='left', va='center', fontsize=10)

    # 3. Contributor Lines of Code
    ax3 = plt.subplot(2, 3, 3)
    net_lines = []
    for author in authors:
        total_added = sum(m['added'] for m in author_stats[author]['modules'].values())
        total_deleted = sum(m['deleted'] for m in author_stats[author]['modules'].values())
        net_lines.append(total_added - total_deleted)

    bars = ax3.barh(authors, net_lines, color=colors[:len(authors)])
    ax3.set_xlabel('Net Lines of Code (Added - Deleted)', fontsize=12)
    ax3.set_title('Net Code Contribution per Person', fontsize=14, fontweight='bold')
    ax3.grid(axis='x', alpha=0.3)

    # Add value labels
    for i, bar in enumerate(bars):
        width = bar.get_width()
        ax3.text(width, bar.get_y() + bar.get_height()/2, f' {int(width):,}',
                ha='left', va='center', fontsize=10)

    # 4. Stacked Bar Chart: Module Contributions by Author
    ax4 = plt.subplot(2, 3, 4)

    # Get all modules
    all_modules = sorted(set(module_stats.keys()))
    module_colors = {mod: colors[i % len(colors)] for i, mod in enumerate(all_modules)}

    # Prepare data
    author_module_data = {author: {mod: 0 for mod in all_modules} for author in authors}
    for author in authors:
        for module, stats in author_stats[author]['modules'].items():
            author_module_data[author][module] = stats['added']

    # Create stacked bars
    bottom = np.zeros(len(authors))
    for module in all_modules:
        values = [author_module_data[author][module] for author in authors]
        ax4.barh(authors, values, left=bottom, label=module, color=module_colors[module])
        bottom += values

    ax4.set_xlabel('Lines Added', fontsize=12)
    ax4.set_title('Module Contributions by Author (Stacked)', fontsize=14, fontweight='bold')
    ax4.legend(loc='center left', bbox_to_anchor=(1, 0.5), fontsize=9)
    ax4.grid(axis='x', alpha=0.3)

    # 5. Module Activity (Added vs Deleted)
    ax5 = plt.subplot(2, 3, 5)
    module_names_sorted = sorted(module_stats.keys(),
                                  key=lambda m: module_stats[m]['added'],
                                  reverse=True)
    module_lines_sorted = [module_stats[m]['added'] for m in module_names_sorted]
    module_deleted_sorted = [module_stats[m]['deleted'] for m in module_names_sorted]

    x = np.arange(len(module_names_sorted))
    width = 0.35

    bars1 = ax5.bar(x - width/2, module_lines_sorted, width, label='Added', color='#2ecc71')
    bars2 = ax5.bar(x + width/2, module_deleted_sorted, width, label='Deleted', color='#e74c3c')

    ax5.set_ylabel('Lines of Code', fontsize=12)
    ax5.set_title('Module Activity (Added vs Deleted)', fontsize=14, fontweight='bold')
    ax5.set_xticks(x)
    ax5.set_xticklabels(module_names_sorted, rotation=45, ha='right')
    ax5.legend()
    ax5.grid(axis='y', alpha=0.3)

    # 6. Top Contributors Summary Table
    ax6 = plt.subplot(2, 3, 6)
    ax6.axis('tight')
    ax6.axis('off')

    # Prepare table data
    table_data = [['Contributor', 'Commits', 'Added', 'Deleted', 'Net']]
    for author in sorted(authors, key=lambda a: author_stats[a]['commits'], reverse=True):
        total_added = sum(m['added'] for m in author_stats[author]['modules'].values())
        total_deleted = sum(m['deleted'] for m in author_stats[author]['modules'].values())
        table_data.append([
            author,
            str(author_stats[author]['commits']),
            f"{total_added:,}",
            f"{total_deleted:,}",
            f"{total_added - total_deleted:,}"
        ])

    table = ax6.table(cellText=table_data, cellLoc='left', loc='center',
                      colWidths=[0.25, 0.15, 0.2, 0.2, 0.2])
    table.auto_set_font_size(False)
    table.set_fontsize(10)
    table.scale(1, 2)

    # Style header row
    for i in range(len(table_data[0])):
        table[(0, i)].set_facecolor('#3498db')
        table[(0, i)].set_text_props(weight='bold', color='white')

    ax6.set_title('Contribution Summary', fontsize=14, fontweight='bold', pad=20)

    plt.tight_layout()
    return fig

def main():
    print("Parsing git log statistics...")
    commits = parse_git_log(r'D:\NUS\gomoku-all\gomuku-backend\gitlog_numstat.txt')
    print(f"Found {len(commits)} commits")

    print("Analyzing contributions...")
    author_stats, module_stats = analyze_contributions(commits)

    print(f"Contributors: {len(author_stats)}")
    print(f"Modules: {len(module_stats)}")

    print("Creating visualizations...")
    fig = create_visualizations(author_stats, module_stats)

    output_file = r'D:\NUS\gomoku-all\gomuku-backend\git-contribution-visualization.png'
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Visualization saved to {output_file}")

    # Print summary
    print("\n" + "="*60)
    print("SUMMARY")
    print("="*60)
    print(f"\nTotal commits: {len(commits)}")
    print(f"\nContributors:")
    for author, stats in sorted(author_stats.items(), key=lambda x: x[1]['commits'], reverse=True):
        total_added = sum(m['added'] for m in stats['modules'].values())
        total_deleted = sum(m['deleted'] for m in stats['modules'].values())
        print(f"  {author}: {stats['commits']} commits, +{total_added:,} -{total_deleted:,} lines")

    print(f"\nModules:")
    for module, stats in sorted(module_stats.items(), key=lambda x: x[1]['added'], reverse=True):
        print(f"  {module}: +{stats['added']:,} -{stats['deleted']:,} lines")

if __name__ == '__main__':
    main()
