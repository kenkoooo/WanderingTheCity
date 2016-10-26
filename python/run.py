import subprocess
import sys
import time


def main():
    min_seed = 117
    num = 100
    score_dict = {}

    start = time.time()
    for seed in range(min_seed, min_seed + num):
        output = subprocess.check_output(["java", "-cp", "out/production/WanderingTheCity",
                                          "WanderingTheCityVis", "-seed", str(seed)]).decode("ascii")
        # 出力をパース
        lines = output.split("\n")
        score = float([line for line in lines if "Score = " in line][0].replace("Score = ", ""))
        s = float([line for line in lines if "S = " in line][0].replace("S = ", ""))
        score_dict[seed] = int(score / s / s * 10000) / 100.0

        # 時間
        elapsed_time = time.time() - start
        sec_per_case = elapsed_time / (seed - min_seed + 1)
        estimated_total = sec_per_case * num
        remain_time = estimated_total - elapsed_time

        # プログレスバーを表示
        progress = 1.0 * (seed - min_seed + 1) / num
        print(seed)
        print('\tElapsed\t%.1f s' % elapsed_time)
        print('\tRemain\t%.1f s' % remain_time)

    total = 0.0
    for k, v in score_dict.items():
        print("{seed}:\t{score}".format(seed=k, score=v))
        total += v
    print("Ave.\t{average}".format(average=(total / num)))


if __name__ == '__main__':
    main()
