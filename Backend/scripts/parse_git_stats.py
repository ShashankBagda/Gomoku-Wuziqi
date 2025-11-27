#!/usr/bin/env python3
"""Parse git log numstat output and generate contribution analysis."""

import re
from collections import defaultdict
from pathlib import Path

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

def get_module(filepath):
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

def parse_git_log(filepath):
    """Parse git log numstat output."""
    commits = []
    current_commit = None

    # The delimiter is ASCII 31 (Unit Separator)
    DELIMITER = chr(31)

    with open(filepath, 'r', encoding='utf-8', errors='ignore') as f:
        for line in f:
            line = line.rstrip('\n')

            # Skip empty lines
            if not line:
                continue

            # Check if this is a commit header line (uses ASCII 31 as delimiter)
            # Format: <hash><US><author><US><email><US><date><US><message>
            if DELIMITER in line:
                parts = line.split(DELIMITER)
                if len(parts) >= 5:
                    commit_hash = parts[0]
                    author = parts[1]
                    email = parts[2]
                    date = parts[3]
                    message = DELIMITER.join(parts[4:])  # In case message contains delimiter

                    if current_commit:
                        commits.append(current_commit)

                    current_commit = {
                        'hash': commit_hash,
                        'author': author,
                        'email': email,
                        'date': date,
                        'message': message,
                        'files': []
                    }

            # Check if this is a file stat line (uses tab as delimiter)
            elif '\t' in line and current_commit:
                parts = line.split('\t')
                if len(parts) >= 3:
                    added = parts[0].strip()
                    deleted = parts[1].strip()
                    file_path = parts[2].strip()

                    # Handle binary files (marked with -)
                    added = 0 if added == '-' else int(added)
                    deleted = 0 if deleted == '-' else int(deleted)

                    current_commit['files'].append({
                        'path': file_path,
                        'added': added,
                        'deleted': deleted,
                        'module': get_module(file_path)
                    })

    # Add last commit
    if current_commit:
        commits.append(current_commit)

    return commits

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

def analyze_contributions(commits):
    """Analyze contributions by author and module."""

    # Contributor stats
    contributors = defaultdict(lambda: {
        'commits': 0,
        'modules': defaultdict(lambda: {'added': 0, 'deleted': 0, 'files': set()}),
        'messages': [],
        'emails': set()
    })

    # Module stats
    modules = defaultdict(lambda: {
        'contributors': defaultdict(lambda: {'commits': 0, 'added': 0, 'deleted': 0}),
        'total_added': 0,
        'total_deleted': 0,
        'files': set()
    })

    for commit in commits:
        author = normalize_author(commit['author'], commit['email'])
        contributors[author]['commits'] += 1
        contributors[author]['emails'].add(commit['email'])
        contributors[author]['messages'].append(commit['message'])

        for file_stat in commit['files']:
            module = file_stat['module']
            added = file_stat['added']
            deleted = file_stat['deleted']
            filepath = file_stat['path']

            # Update contributor stats
            contributors[author]['modules'][module]['added'] += added
            contributors[author]['modules'][module]['deleted'] += deleted
            contributors[author]['modules'][module]['files'].add(filepath)

            # Update module stats
            modules[module]['contributors'][author]['commits'] += 1
            modules[module]['contributors'][author]['added'] += added
            modules[module]['contributors'][author]['deleted'] += deleted
            modules[module]['total_added'] += added
            modules[module]['total_deleted'] += deleted
            modules[module]['files'].add(filepath)

    return contributors, modules

def categorize_work(messages, files):
    """Categorize work based on commit messages and files."""
    categories = {
        'Features': [],
        'Bug Fixes': [],
        'CI/CD': [],
        'Documentation': [],
        'Refactoring': [],
        'Testing': [],
        'Configuration': [],
        'Merges': []
    }

    for msg in messages:
        msg_lower = msg.lower()

        if msg.startswith('Merge') or 'merge' in msg_lower:
            categories['Merges'].append(msg)
        elif msg.startswith('feat') or 'feature' in msg_lower or 'add' in msg_lower:
            categories['Features'].append(msg)
        elif msg.startswith('fix') or 'bug' in msg_lower:
            categories['Bug Fixes'].append(msg)
        elif msg.startswith('ci') or 'pipeline' in msg_lower or '.gitlab-ci' in msg:
            categories['CI/CD'].append(msg)
        elif msg.startswith('docs') or 'documentation' in msg_lower:
            categories['Documentation'].append(msg)
        elif msg.startswith('refactor') or 'refactor' in msg_lower:
            categories['Refactoring'].append(msg)
        elif msg.startswith('test') or 'test' in msg_lower:
            categories['Testing'].append(msg)
        elif msg.startswith('chore') or msg.startswith('build') or 'config' in msg_lower:
            categories['Configuration'].append(msg)

    return {k: v for k, v in categories.items() if v}

def generate_markdown_report(commits, contributors, modules):
    """Generate comprehensive markdown report."""

    lines = []
    lines.append("# Git Contribution Analysis")
    lines.append("")
    lines.append(f"**Analysis Date:** 2025-10-31")
    lines.append("")

    # Overview statistics
    lines.append("## Overview Statistics")
    lines.append("")
    lines.append(f"- **Total Commits:** {len(commits)}")
    lines.append(f"- **Total Contributors:** {len(contributors)}")
    lines.append(f"- **Modules:** {len(modules)}")
    lines.append("")

    total_added = sum(m['total_added'] for m in modules.values())
    total_deleted = sum(m['total_deleted'] for m in modules.values())
    lines.append(f"- **Total Lines Added:** {total_added:,}")
    lines.append(f"- **Total Lines Deleted:** {total_deleted:,}")
    lines.append(f"- **Net Lines:** {total_added - total_deleted:+,}")
    lines.append("")

    # Contributors list
    lines.append("### Contributors")
    lines.append("")
    for author in sorted(contributors.keys()):
        emails = ', '.join(contributors[author]['emails'])
        lines.append(f"- **{author}** ({emails})")
    lines.append("")

    # Module overview
    lines.append("### Modules")
    lines.append("")
    sorted_modules = sorted(modules.items(), key=lambda x: x[1]['total_added'] + x[1]['total_deleted'], reverse=True)
    for module, stats in sorted_modules:
        lines.append(f"- **{module}**: {stats['total_added']:,} added, {stats['total_deleted']:,} deleted ({len(stats['files'])} files)")
    lines.append("")

    lines.append("---")
    lines.append("")

    # Per-contributor breakdown
    lines.append("## Per-Contributor Breakdown")
    lines.append("")

    sorted_contributors = sorted(contributors.items(), key=lambda x: x[1]['commits'], reverse=True)

    for author, stats in sorted_contributors:
        lines.append(f"### {author}")
        lines.append("")

        emails = ', '.join(stats['emails'])
        lines.append(f"**Email:** {emails}")
        lines.append("")
        lines.append(f"**Total Commits:** {stats['commits']}")
        lines.append("")

        # Module contributions
        lines.append("#### Contributions by Module")
        lines.append("")
        lines.append("| Module | Lines Added | Lines Deleted | Net Lines | Files Modified |")
        lines.append("|--------|-------------|---------------|-----------|----------------|")

        sorted_module_stats = sorted(stats['modules'].items(),
                                     key=lambda x: x[1]['added'] + x[1]['deleted'],
                                     reverse=True)

        total_author_added = 0
        total_author_deleted = 0

        for module, module_stats in sorted_module_stats:
            added = module_stats['added']
            deleted = module_stats['deleted']
            net = added - deleted
            files = len(module_stats['files'])

            total_author_added += added
            total_author_deleted += deleted

            net_str = f"+{net:,}" if net >= 0 else f"{net:,}"
            lines.append(f"| {module} | {added:,} | {deleted:,} | {net_str} | {files} |")

        total_net = total_author_added - total_author_deleted
        total_net_str = f"+{total_net:,}" if total_net >= 0 else f"{total_net:,}"
        lines.append(f"| **TOTAL** | **{total_author_added:,}** | **{total_author_deleted:,}** | **{total_net_str}** | - |")
        lines.append("")

        # Categorize work
        categories = categorize_work(stats['messages'], [])

        if categories:
            lines.append("#### Key Contributions")
            lines.append("")

            for category, messages in categories.items():
                if messages:
                    lines.append(f"**{category}** ({len(messages)} commits)")
                    # Show up to 5 most recent unique messages
                    seen = set()
                    shown = 0
                    for msg in messages[:20]:  # Check first 20
                        if msg not in seen and shown < 5:
                            lines.append(f"- {msg}")
                            seen.add(msg)
                            shown += 1
                    if len(messages) > 5:
                        lines.append(f"  *(and {len(messages) - shown} more)*")
                    lines.append("")

        lines.append("---")
        lines.append("")

    # Per-module breakdown
    lines.append("## Per-Module Breakdown")
    lines.append("")

    for module, stats in sorted_modules:
        lines.append(f"### {module}")
        lines.append("")

        lines.append(f"**Total Lines Added:** {stats['total_added']:,}")
        lines.append(f"**Total Lines Deleted:** {stats['total_deleted']:,}")
        lines.append(f"**Net Lines:** {stats['total_added'] - stats['total_deleted']:+,}")
        lines.append(f"**Files Modified:** {len(stats['files'])}")
        lines.append("")

        lines.append("#### Contributors")
        lines.append("")
        lines.append("| Contributor | Commits | Lines Added | Lines Deleted | Net Lines |")
        lines.append("|-------------|---------|-------------|---------------|-----------|")

        sorted_module_contributors = sorted(stats['contributors'].items(),
                                           key=lambda x: x[1]['added'] + x[1]['deleted'],
                                           reverse=True)

        for contributor, contrib_stats in sorted_module_contributors:
            commits = contrib_stats['commits']
            added = contrib_stats['added']
            deleted = contrib_stats['deleted']
            net = added - deleted
            net_str = f"+{net:,}" if net >= 0 else f"{net:,}"

            lines.append(f"| {contributor} | {commits} | {added:,} | {deleted:,} | {net_str} |")

        lines.append("")
        lines.append("---")
        lines.append("")

    return '\n'.join(lines)

def main():
    """Main function."""
    input_file = r'D:\NUS\gomoku-all\gomuku-backend\gitlog_numstat.txt'
    output_file = r'D:\NUS\gomoku-all\gomuku-backend\git-contribution-analysis.md'

    print("Parsing git log...")
    commits = parse_git_log(input_file)
    print(f"Found {len(commits)} commits")

    print("Analyzing contributions...")
    contributors, modules = analyze_contributions(commits)
    print(f"Found {len(contributors)} contributors and {len(modules)} modules")

    print("Generating markdown report...")
    report = generate_markdown_report(commits, contributors, modules)

    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(report)

    print(f"Report saved to {output_file}")
    print(f"\nSummary:")
    print(f"  - {len(commits)} commits")
    print(f"  - {len(contributors)} contributors")
    print(f"  - {len(modules)} modules")

if __name__ == '__main__':
    main()