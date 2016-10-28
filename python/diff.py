import json
import subprocess
import time
import argparse
import datetime
import numpy


def main(a):
    if not a.after or not a.before:
        return
    before = json.load(open(a.before))
    after = json.load(open(a.after))
    diff = {}

    for r in after:
        diff[r["seed"]] = {"after": r["score"] / r["g_cost"]}

    for r in before:
        diff[r["seed"]]["before"] = r["score"] / r["g_cost"]

    for seed, d in diff.items():
        ds = d["after"] - d["before"]
        print("{seed}:\t{before}\t->\t{after}\t{diff}".format(
            seed=seed,
            before=("%.3f" % d["before"]),
            after=("%.3f" % d["after"]),
            diff=("%.3f" % ds),
        ))
    average_after = numpy.average([d["after"] for d in diff.values()])
    average_before = numpy.average([d["before"] for d in diff.values()])
    print("Ave.\t{before}\t->\t{after}".format(after=("%.5f" % average_after), before=("%.5f" % average_before)))


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
