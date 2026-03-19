"""

TESTE FEITO COM IA, NÂO HÀ GARANTIA QUE O SERVIDOR FUNCIONE CORRETAMENTE COM ESTE CLIENTE. APENAS PARA TESTES BÁSICOS E DEBUGGING.


"""










"""Cliente UDP simples para testar o servidor Snake.

Uso:
  python test_client.py [HOST] [PORT] [NAME]

Exemplo:
  python test_client.py 127.0.0.1 3000 Teste

O cliente:
- envia um `join` com nome
- escuta mensagens do servidor (estado) e imprime o JSON recebido
- envia inputs aleatórios (UP/DOWN/LEFT/RIGHT) a cada 0.5s
- encerra após 10 segundos e envia `leave`
"""

import json
import random
import socket
import sys
import time

HOST = sys.argv[1] if len(sys.argv) > 1 else "127.0.0.1"
PORT = int(sys.argv[2]) if len(sys.argv) > 2 else 3000
NAME = sys.argv[3] if len(sys.argv) > 3 else "Tester"

DIRECTIONS = ["UP", "DOWN", "LEFT", "RIGHT"]

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.settimeout(1.0)

addr = (HOST, PORT)

# Envia join
msg = {"type": "join", "name": NAME}
sock.sendto(json.dumps(msg).encode("utf-8"), addr)
print("[CLIENT] Enviado join ->", msg)

start = time.time()
next_input = start + 0.5

try:
    while True:
        now = time.time()
        if now >= next_input:
            # envia input aleatório
            dir = random.choice(DIRECTIONS)
            msg = {"type": "input", "dir": dir}
            sock.sendto(json.dumps(msg).encode("utf-8"), addr)
            print("[CLIENT] Enviado input ->", msg)
            next_input = now + 0.5

        try:
            # Aumenta buffer para evitar erros de datagrama maior que o buffer
            data, _ = sock.recvfrom(65535)
            state = json.loads(data.decode("utf-8"))
            print("[CLIENT] Estado recebido ->", json.dumps(state, indent=2))
        except socket.timeout:
            pass
        except OSError as e:
            print(f"[CLIENT] Erro ao receber: {e}")
            break

        if now - start > 10:
            break

finally:
    # envia leave
    msg = {"type": "leave"}
    sock.sendto(json.dumps(msg).encode("utf-8"), addr)
    print("[CLIENT] Enviado leave")
    sock.close()
