import json
import subprocess
import time
import argparse
import datetime


def main(a):
    if not a.json:
        return
    data = json.load(open(a.json))
    data = sorted(data, key=lambda x: x["score"] / x["g_cost"])
    for result in data:
        print("{seed}:".format(seed=result["seed"]))
        print("S = {S}".format(S=result["S"]))
        print("guess = {guess}".format(guess=result["guess"]))
        print("look = {look}".format(look=result["look"]))
        print("score = {score}".format(score=("%.5f" % (result["score"] / result["g_cost"]))))
        print()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='This script is ...')
    parser.add_argument('-f', '--json',
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
