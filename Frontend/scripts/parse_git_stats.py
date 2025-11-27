#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re
from collections import defaultdict
from datetime import datetime
import os

def parse_mailmap(mailmap_file='.mailmap'):
    """Parse .mailmap file to create email/name mappings."""
    mailmap = {}

    if not os.path.exists(mailmap_file):
        return mailmap

    with open(mailmap_file, 'r', encoding='utf-8', errors='ignore') as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith('#'):
                continue

            # Parse mailmap format: Proper Name <proper@email> Commit Name <commit@email>
            # or: Proper Name <proper@email> <commit@email>
            # or: Proper Name <proper@email> Commit Name
            match = re.match(r'^([^<]+?)\s*<([^>]+)>\s+(.+)$', line)
            if match:
                proper_name = match.group(1).strip()
                proper_email = match.group(2).strip()
                rest = match.group(3).strip()

                # Extract commit name and/or email from rest
                commit_email_match = re.search(r'<([^>]+)>', rest)
                commit_name_match = re.match(r'^([^<]+)', rest)

                if commit_email_match:
                    commit_email = commit_email_match.group(1).strip()
                    mailmap[commit_email] = (proper_name, proper_email)

                if commit_name_match:
                    commit_name = commit_name_match.group(1).strip()
                    mailmap[commit_name] = (proper_name, proper_email)

    return mailmap

def parse_git_log(filename='gitlog_numstat.txt', mailmap_file='.mailmap'):
    """Parse git log numstat output and extract contribution statistics."""

    # Parse mailmap for author name/email mappings
    mailmap = parse_mailmap(mailmap_file)

    stats = defaultdict(lambda: {
        'commits': 0,
        'additions': 0,
        'deletions': 0,
        'files_changed': 0,
        'files': defaultdict(lambda: {'additions': 0, 'deletions': 0}),
        'dates': []
    })

    current_commit = None

    with open(filename, 'r', encoding='utf-8', errors='ignore') as f:
        for line in f:
            line = line.strip()

            # Parse commit metadata
            if line.startswith('commit:'):
                parts = line.split('|')
                commit_hash = parts[0].split(':')[1]
                author = parts[1].split(':')[1]
                email = parts[2].split(':')[1]
                date_str = parts[3].split('date:')[1]

                # Apply mailmap to normalize author name
                if email in mailmap:
                    author = mailmap[email][0]
                elif author in mailmap:
                    author = mailmap[author][0]

                current_commit = {
                    'hash': commit_hash,
                    'author': author,
                    'email': email,
                    'date': date_str
                }

                # Count commit
                stats[author]['commits'] += 1
                stats[author]['dates'].append(date_str)

            # Parse file changes (numstat)
            elif line and current_commit and '\t' in line:
                parts = line.split('\t')
                if len(parts) >= 3:
                    try:
                        additions = int(parts[0]) if parts[0] != '-' else 0
                        deletions = int(parts[1]) if parts[1] != '-' else 0
                        filename = parts[2]

                        author = current_commit['author']
                        stats[author]['additions'] += additions
                        stats[author]['deletions'] += deletions
                        stats[author]['files_changed'] += 1
                        stats[author]['files'][filename]['additions'] += additions
                        stats[author]['files'][filename]['deletions'] += deletions
                    except ValueError:
                        pass  # Skip binary files or other non-numeric entries

    return stats

def generate_report(stats, output_file='git-contribution-analysis.md'):
    """Generate a markdown report from the statistics."""

    # Sort authors by total contributions (additions + deletions)
    sorted_authors = sorted(
        stats.items(),
        key=lambda x: x[1]['additions'] + x[1]['deletions'],
        reverse=True
    )

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write("# Git Contribution Analysis\n\n")
        f.write(f"Generated on: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}\n\n")

        # Overall statistics
        f.write("## Overall Statistics\n\n")
        total_commits = sum(s['commits'] for s in stats.values())
        total_additions = sum(s['additions'] for s in stats.values())
        total_deletions = sum(s['deletions'] for s in stats.values())
        total_authors = len(stats)

        f.write(f"- **Total Authors**: {total_authors}\n")
        f.write(f"- **Total Commits**: {total_commits}\n")
        f.write(f"- **Total Lines Added**: {total_additions:,}\n")
        f.write(f"- **Total Lines Deleted**: {total_deletions:,}\n")
        f.write(f"- **Net Lines**: {total_additions - total_deletions:,}\n\n")

        # Per-author statistics
        f.write("## Contributions by Author\n\n")
        f.write("| Author | Commits | Lines Added | Lines Deleted | Net Lines | Files Changed |\n")
        f.write("|--------|---------|-------------|---------------|-----------|---------------|\n")

        for author, author_stats in sorted_authors:
            net_lines = author_stats['additions'] - author_stats['deletions']
            f.write(f"| {author} | {author_stats['commits']} | "
                   f"{author_stats['additions']:,} | {author_stats['deletions']:,} | "
                   f"{net_lines:,} | {author_stats['files_changed']} |\n")

        # Detailed breakdown per author
        f.write("\n## Detailed Breakdown\n\n")

        for author, author_stats in sorted_authors:
            f.write(f"### {author}\n\n")
            f.write(f"- **Commits**: {author_stats['commits']}\n")
            f.write(f"- **Lines Added**: {author_stats['additions']:,}\n")
            f.write(f"- **Lines Deleted**: {author_stats['deletions']:,}\n")
            f.write(f"- **Net Contribution**: {author_stats['additions'] - author_stats['deletions']:,}\n")
            f.write(f"- **Files Changed**: {author_stats['files_changed']}\n\n")

            # Top modified files
            top_files = sorted(
                author_stats['files'].items(),
                key=lambda x: x[1]['additions'] + x[1]['deletions'],
                reverse=True
            )[:10]

            if top_files:
                f.write("#### Top Modified Files\n\n")
                f.write("| File | Lines Added | Lines Deleted |\n")
                f.write("|------|-------------|---------------|\n")
                for filename, file_stats in top_files:
                    f.write(f"| {filename} | {file_stats['additions']:,} | {file_stats['deletions']:,} |\n")
                f.write("\n")

        # Contribution percentage
        f.write("## Contribution Percentage\n\n")
        f.write("| Author | Commit % | Lines Added % | Lines Deleted % |\n")
        f.write("|--------|----------|---------------|------------------|\n")

        for author, author_stats in sorted_authors:
            commit_pct = (author_stats['commits'] / total_commits * 100) if total_commits > 0 else 0
            add_pct = (author_stats['additions'] / total_additions * 100) if total_additions > 0 else 0
            del_pct = (author_stats['deletions'] / total_deletions * 100) if total_deletions > 0 else 0

            f.write(f"| {author} | {commit_pct:.1f}% | {add_pct:.1f}% | {del_pct:.1f}% |\n")

    print(f"Report generated: {output_file}")
    return sorted_authors

if __name__ == '__main__':
    print("Parsing git statistics...")
    stats = parse_git_log()
    print(f"Found {len(stats)} contributors")

    print("Generating report...")
    generate_report(stats)
    print("Done!")
