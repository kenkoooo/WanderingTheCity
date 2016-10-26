import json
import subprocess
import time
import argparse
import datetime


def main(a):
    if not a.after or not a.before:
        return
    before = json.load(open(a.before))
    after = json.load(open(a.after))
    diff = {}

    for r in after:
        diff[r["seed"]] = {"after": r["score"]}

    for r in before:
        diff[r["seed"]]["before"] = r["score"]

    total = 0
    for seed, d in diff.items():
        total += d["after"]
        ds = d["after"] - d["before"]
        print("{seed}:\t{before}\t->\t{after}\t{diff}".format(
            seed=seed,
            before=("%.2e" % d["before"]),
            after=("%.2e" % d["after"]),
            diff=ds
        ))
    print(total / len(after))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='This script is ...')
    parser.add_argument('-b', '--before',
                        action='store',
                        nargs=None,
                        const=None,
                        default=None,
                        type=str,
                        choices=None,
                        help='Directory path where your taken photo files are located.',
                        metavar=None)
    parser.add_argument('-a', '--after',
                        action='store',
                        nargs=None,
                        const=None,
                        default=None,
                        type=str,
                        choices=None,
                        help='Directory path where your taken photo files are located.',
                        metavar=None)
    args = parser.parse_args()
    main(args)
