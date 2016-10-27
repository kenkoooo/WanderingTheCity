import json
import subprocess
from subprocess import TimeoutExpired
import time
import argparse
import datetime
import multiprocessing as mp

LOG_DIR = "log/"


def command(seed, vis=False):
    ret = ["java", "-cp", "out/production/WanderingTheCity",
           "WanderingTheCityVis", "-seed", str(seed)]
    if vis:
        ret.append("-vis")
    return ret


def simulate(seed):
    try:
        start = time.time()
        output = subprocess.check_output(command(seed), timeout=20).decode("ascii")
        # 出力をパース
        lines = output.split("\n")
        score = float([line for line in lines if "Score = " in line][0].replace("Score = ", ""))
        s = int([line for line in lines if "S = " in line][0].replace("S = ", ""))
        look = int(
            [line for line in lines if "Number of look() calls = " in line][0].replace("Number of look() calls = ", ""))
        guess = int([line for line in lines if "Number of incorrect guess() calls = " in line][0].replace(
            "Number of incorrect guess() calls = ", ""))

        result = {
            "seed": seed,
            "score": score,
            "S": s,
            "look": look,
            "guess": guess}
        exec_time = time.time() - start
        print("{seed}:\t{t} s".format(seed=seed, t=("%.2f" % exec_time)))
    except TimeoutExpired:
        print("TLE in {seed}".format(seed=seed))
        return {}
    return result


def batch(num):
    min_seed = 117
    pool = mp.Pool(4)
    callback = pool.map(simulate, range(min_seed, min_seed + num))

    total = 0.0
    for r in callback:
        total += r["score"]
    print("Ave.\t{average}".format(average=(total / num)))

    now = datetime.datetime.now()
    filename = LOG_DIR + now.strftime("%Y-%m-%d-%H-%M") + "-{min_seed}-{num}".format(min_seed=min_seed,
                                                                                     num=num) + ".json"
    f = open(filename, "w")
    json.dump(callback, f, ensure_ascii=False, indent=4, sort_keys=True, separators=(',', ': '))
    print(filename)


def visualize(seed):
    print(subprocess.check_output(command(seed, True)).decode("ascii"))


def main(a):
    if not a.seed:
        batch(a.num)
    else:
        visualize(a.seed)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='This script is ...')
    parser.add_argument('-s', '--seed',
                        action='store',
                        nargs=None,
                        const=None,
                        default=None,
                        type=str,
                        choices=None,
                        help='Directory path where your taken photo files are located.',
                        metavar=None)
    parser.add_argument('-n', '--num',
                        action='store',
                        nargs=None,
                        const=None,
                        default=100,
                        type=int,
                        choices=None,
                        help='Directory path where your taken photo files are located.',
                        metavar=None)
    args = parser.parse_args()
    main(args)
