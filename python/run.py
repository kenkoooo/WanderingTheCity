import subprocess


def main():
    min_seed = 117
    num = 100
    for seed in range(min_seed, min_seed + num):
        output = subprocess.check_output(["java", "-cp", "out/production/WanderingTheCity",
                                          "WanderingTheCityVis", "-seed", str(seed)])
        print(output.decode("ascii"))


if __name__ == '__main__':
    main()
