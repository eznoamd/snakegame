"""

TESTE FEITO COM IA, NÂO HÀ GARANTIA QUE O SERVIDOR FUNCIONE CORRETAMENTE COM ESTE CLIENTE. APENAS PARA TESTES BÁSICOS E DEBUGGING.


"""






import socket
import json
import time

SERVER_ADDR = ("127.0.0.1", 3000)

def main():
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    
    # bind em porta aleatória (igual Rust)
    sock.bind(("0.0.0.0", 0))
    print("Cliente rodando em:", sock.getsockname())

    # timeout pra não travar
    sock.settimeout(2)

    # envia join
    join_msg = {
        "type": "join",
        "name": "PythonTest"
    }

    sock.sendto(json.dumps(join_msg).encode(), SERVER_ADDR)
    print("Join enviado")

    last_input = time.time()

    while True:
        try:
            data, addr = sock.recvfrom(4096)
            text = data.decode()

            print("\n=== RECEBIDO ===")
            print(text)

            # tenta parsear
            try:
                obj = json.loads(text)
                print("JSON válido")

                self_data = obj.get("self")

                if self_data:
                    alive = self_data.get("alive", True)
                    print("alive:", alive)

                    if not alive:
                        print(">>> PLAYER MORREU — ENCERRANDO <<<")
                        break
                        
            except Exception as e:
                print("JSON inválido:", e)

        except socket.timeout:
            print("... aguardando dados ...")

        # manda input de vez em quando
        if time.time() - last_input > 1:
            input_msg = {
                "type": "input",
                "dir": "RIGHT"
            }
            sock.sendto(json.dumps(input_msg).encode(), SERVER_ADDR)
            print("→ input enviado")
            last_input = time.time()


if __name__ == "__main__":
    main()