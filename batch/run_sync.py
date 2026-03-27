"""
Main entry point: syncs deputies first, then votes.

Usage:
    python run_sync.py          # Sync votes from last 7 days
    python run_sync.py 30       # Sync votes from last 30 days
"""

import sys
from sync_deputies import sync as sync_deputies
from sync_votes import sync as sync_votes


def main():
    days = int(sys.argv[1]) if len(sys.argv) > 1 else 7

    print("=" * 60)
    print("  De Olho Neles — Batch Sync")
    print("=" * 60)
    print()

    deputies_created = sync_deputies()

    print()
    print("-" * 60)
    print()

    proposals_created, votes_created = sync_votes(days_back=days)

    print()
    print("=" * 60)
    print(f"  Total: {deputies_created} deputies, {proposals_created} proposals, {votes_created} votes")
    print("=" * 60)


if __name__ == "__main__":
    main()
