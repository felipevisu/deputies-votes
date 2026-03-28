"""
Main entry point: syncs deputies, then votes, then proposals.

Usage:
    python run_sync.py          # Sync votes from last 7 days
    python run_sync.py 30       # Sync votes from last 30 days
"""

import sys
from sync_deputies import sync as sync_deputies
from sync_votes import sync as sync_votes
from sync_proposals import sync as sync_proposals


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

    activities_created, votes_created = sync_votes(days_back=days)

    print()
    print("-" * 60)
    print()

    proposals_created = sync_proposals()

    print()
    print("=" * 60)
    print(f"  Total: {deputies_created} deputies, {activities_created} activities,")
    print(f"         {votes_created} votes, {proposals_created} proposals")
    print("=" * 60)


if __name__ == "__main__":
    main()
