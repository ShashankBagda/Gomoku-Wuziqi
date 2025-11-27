#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import matplotlib.pyplot as plt
import matplotlib
import numpy as np
from parse_git_stats import parse_git_log
from collections import defaultdict

# Use a non-interactive backend for server environments
matplotlib.use('Agg')

def get_directory_from_path(filepath):
    """Determine which directory/module a file belongs to"""
    if not filepath or filepath == '-':
        return 'config/root'

    # Main source directories
    if filepath.startswith('src/pages/'):
        return 'pages'
    elif filepath.startswith('src/components/'):
        return 'components'
    elif filepath.startswith('src/utils/'):
        return 'utils'
    elif filepath.startswith('src/api/'):
        return 'api'
    elif filepath.startswith('src/styles/') or filepath.startswith('src/css/'):
        return 'styles'
    elif filepath.startswith('src/assets/'):
        return 'assets'
    elif filepath.startswith('src/'):
        return 'src/other'
    elif filepath.startswith('public/'):
        return 'public'
    elif 'package' in filepath.lower() and '.json' in filepath.lower():
        return 'dependencies'
    elif filepath.startswith('test/') or '__tests__' in filepath or '.test.' in filepath:
        return 'tests'
    else:
        return 'config/root'

def analyze_contributions_by_directory(stats):
    """Analyze contributions by author and directory"""
    author_dir_stats = defaultdict(lambda: defaultdict(lambda: {'added': 0, 'deleted': 0}))
    dir_stats = defaultdict(lambda: {'added': 0, 'deleted': 0})

    for author, author_data in stats.items():
        for filepath, file_data in author_data['files'].items():
            directory = get_directory_from_path(filepath)

            # Update author-directory stats
            author_dir_stats[author][directory]['added'] += file_data['additions']
            author_dir_stats[author][directory]['deleted'] += file_data['deletions']

            # Update directory stats
            dir_stats[directory]['added'] += file_data['additions']
            dir_stats[directory]['deleted'] += file_data['deletions']

    return dict(author_dir_stats), dict(dir_stats)

def create_visualizations(stats, output_file='git-contribution-visualization.png'):
    """Create comprehensive visualization matching backend format"""

    # Analyze by directory
    author_dir_stats, dir_stats = analyze_contributions_by_directory(stats)

    # Set style
    plt.style.use('default')
    colors = plt.cm.Set3(np.linspace(0, 1, 12))

    # Sort authors by commits
    sorted_authors = sorted(stats.items(), key=lambda x: x[1]['commits'], reverse=True)
    authors = [author for author, _ in sorted_authors]

    # Create figure with 2x3 grid
    fig = plt.figure(figsize=(20, 12))

    # 1. Directory Distribution Pie Chart (top left)
    ax1 = plt.subplot(2, 3, 1)
    dir_names = list(dir_stats.keys())
    dir_lines = [s['added'] for s in dir_stats.values()]

    wedges, texts, autotexts = ax1.pie(dir_lines, labels=dir_names, autopct='%1.1f%%',
                                         colors=colors[:len(dir_names)], startangle=90)
    ax1.set_title('Code Distribution by Directory\n(Lines Added)', fontsize=14, fontweight='bold')

    for autotext in autotexts:
        autotext.set_color('black')
        autotext.set_fontweight('bold')
        autotext.set_fontsize(9)

    # 2. Commits per Contributor (top middle)
    ax2 = plt.subplot(2, 3, 2)
    commits = [s['commits'] for _, s in sorted_authors]

    bars = ax2.barh(authors, commits, color=colors[:len(authors)])
    ax2.set_xlabel('Number of Commits', fontsize=12)
    ax2.set_title('Commits per Contributor', fontsize=14, fontweight='bold')
    ax2.grid(axis='x', alpha=0.3)

    for i, bar in enumerate(bars):
        width = bar.get_width()
        ax2.text(width, bar.get_y() + bar.get_height()/2, f' {int(width)}',
                ha='left', va='center', fontsize=10)

    # 3. Net Code Contribution (top right)
    ax3 = plt.subplot(2, 3, 3)
    net_lines = [s['additions'] - s['deletions'] for _, s in sorted_authors]

    bars = ax3.barh(authors, net_lines, color=colors[:len(authors)])
    ax3.set_xlabel('Net Lines of Code (Added - Deleted)', fontsize=12)
    ax3.set_title('Net Code Contribution per Person', fontsize=14, fontweight='bold')
    ax3.grid(axis='x', alpha=0.3)

    for i, bar in enumerate(bars):
        width = bar.get_width()
        ax3.text(width, bar.get_y() + bar.get_height()/2, f' {int(width):,}',
                ha='left', va='center', fontsize=10)

    # 4. Stacked Bar: Directory Contributions by Author (bottom left)
    ax4 = plt.subplot(2, 3, 4)

    all_dirs = sorted(set(dir_stats.keys()))
    dir_colors = {d: colors[i % len(colors)] for i, d in enumerate(all_dirs)}

    author_dir_data = {author: {d: 0 for d in all_dirs} for author in authors}
    for author in authors:
        for directory, dstats in author_dir_stats[author].items():
            author_dir_data[author][directory] = dstats['added']

    bottom = np.zeros(len(authors))
    for directory in all_dirs:
        values = [author_dir_data[author][directory] for author in authors]
        ax4.barh(authors, values, left=bottom, label=directory, color=dir_colors[directory])
        bottom += values

    ax4.set_xlabel('Lines Added', fontsize=12)
    ax4.set_title('Directory Contributions by Author (Stacked)', fontsize=14, fontweight='bold')
    ax4.legend(loc='center left', bbox_to_anchor=(1, 0.5), fontsize=9)
    ax4.grid(axis='x', alpha=0.3)

    # 5. Directory Activity (Added vs Deleted) (bottom middle)
    ax5 = plt.subplot(2, 3, 5)
    dir_names_sorted = sorted(dir_stats.keys(), key=lambda d: dir_stats[d]['added'], reverse=True)
    dir_added_sorted = [dir_stats[d]['added'] for d in dir_names_sorted]
    dir_deleted_sorted = [dir_stats[d]['deleted'] for d in dir_names_sorted]

    x = np.arange(len(dir_names_sorted))
    width = 0.35

    bars1 = ax5.bar(x - width/2, dir_added_sorted, width, label='Added', color='#2ecc71')
    bars2 = ax5.bar(x + width/2, dir_deleted_sorted, width, label='Deleted', color='#e74c3c')

    ax5.set_ylabel('Lines of Code', fontsize=12)
    ax5.set_title('Directory Activity (Added vs Deleted)', fontsize=14, fontweight='bold')
    ax5.set_xticks(x)
    ax5.set_xticklabels(dir_names_sorted, rotation=45, ha='right')
    ax5.legend()
    ax5.grid(axis='y', alpha=0.3)

    # 6. Contribution Summary Table (bottom right)
    ax6 = plt.subplot(2, 3, 6)
    ax6.axis('tight')
    ax6.axis('off')

    table_data = [['Contributor', 'Commits', 'Added', 'Deleted', 'Net']]
    for author, author_stats in sorted_authors:
        table_data.append([
            author,
            str(author_stats['commits']),
            f"{author_stats['additions']:,}",
            f"{author_stats['deletions']:,}",
            f"{author_stats['additions'] - author_stats['deletions']:,}"
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
    plt.savefig(output_file, dpi=300, bbox_inches='tight')
    print(f"Visualization saved: {output_file}")

if __name__ == '__main__':
    print("Parsing git statistics...")
    stats = parse_git_log()
    print(f"Found {len(stats)} contributors")

    print("Creating visualizations...")
    create_visualizations(stats)
    print("Done!")
