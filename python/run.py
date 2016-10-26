import subprocess
import time
import argparse


def command(seed, vis=False):
    ret = ["java", "-cp", "out/production/WanderingTheCity",
           "WanderingTheCityVis", "-seed", str(seed)]
    if vis:
        ret.append("-vis")
    return ret


def batch():
    min_seed = 117
    num = 100
    score_dict = {}

    start = time.time()
    for seed in range(min_seed, min_seed + num):
        output = subprocess.check_output(command(seed)).decode("ascii")
        # 出力をパース
        lines = output.split("\n")
        score = float([line for line in lines if "Score = " in line][0].replace("Score = ", ""))
        s = int([line for line in lines if "S = " in line][0].replace("S = ", ""))
        look = int(
            [line for line in lines if "Number of look() calls = " in line][0].replace("Number of look() calls = ", ""))
        guess = int([line for line in lines if "Number of incorrect guess() calls = " in line][0].replace(
            "Number of incorrect guess() calls = ", ""))

        score_dict[seed] = (int(score), s, look, guess)

        # 時間
        elapsed_time = time.time() - start
        sec_per_case = elapsed_time / (seed - min_seed + 1)
        estimated_total = sec_per_case * num
        remain_time = estimated_total - elapsed_time

        print(str(seed) + (' Elapsed\t%.1f s' % elapsed_time) + ('\tRemain\t%.1f s' % remain_time))

    total = 0.0
    for k, v in score_dict.items():
        print("{seed}:\tS = {S}\tlook = {look}\tguess = {guess}\tscore = {score}".format(seed=k,
                                                                                         score=v[0],
                                                                                         S=v[1],
                                                                                         look=v[2],
                                                                                         guess=v[3]))
        total += v[0]
    print("Ave.\t{average}".format(average=(total / num)))


def visualize(seed):
    print(subprocess.check_output(command(seed, True)).decode("ascii"))


def main(a):
    if not a.seed:
        batch()
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
    args = parser.parse_args()
    main(args)
