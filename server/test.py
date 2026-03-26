import argparse
import subprocess
import sys

#
# Este script é um runner de testes para o projeto. Ele permite rodar testes específicos ou todos os testes de uma vez.
# Testes disponíveis:

# Caminho até os testes
PATH_TO_TESTS = "tests/"

# Mapiamento dos testes disponiveis
AVAILABLE_TESTS = {
    "client": "test_client.py",
    "server": "test_server.py"
}

def run_test(test_name):
    if test_name == "all":
        print("Rodando todos os testes...\n")
        for name, path in AVAILABLE_TESTS.items():
            complete_path = PATH_TO_TESTS + path
            print(f"==> {name}")
            subprocess.run([sys.executable, complete_path])
    else:
        path = PATH_TO_TESTS + AVAILABLE_TESTS.get(test_name)

        if not path:
            print(f"Teste '{test_name}' não encontrado.")
            print(f"Disponíveis: {', '.join(AVAILABLE_TESTS.keys())}")
            return

        print(f"Rodando teste: {test_name}\n")
        subprocess.run([sys.executable, path])

def main():
    parser = argparse.ArgumentParser(
        description="Runner de testes do projeto"
    )

    parser.add_argument(
        "--test",
        type=str,
        required=True,
        help="Qual teste rodar (client)"
    )

    args = parser.parse_args()

    run_test(args.test)

if __name__ == "__main__":
    main()