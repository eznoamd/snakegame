import socket
import json
import threading
import time

SERVER_IP = "127.0.0.1"
SERVER_PORT = 3000

def listen_responses(sock):
    """Thread para escutar o que o servidor envia de volta"""
    print("[UDP] Escutando respostas do servidor...")
    sock.settimeout(5) # Timeout de 5 segundos para o teste não travar
    try:
        while True:
            data, addr = sock.recvfrom(4096)
            message = data.decode('utf-8')
            print(f"\n[RECEBIDO de {addr}]:")
            # Tenta formatar o JSON para ficar legível
            try:
                parsed = json.loads(message)
                print(json.dumps(parsed, indent=2))
            except:
                print(message)
    except socket.timeout:
        print("\n[AVISO] Timeout: Nenhuma mensagem recebida do servidor nos últimos 5 segundos.")
    except Exception as e:
        print(f"\n[ERRO] na escuta: {e}")

def run_test():
    # Cria o socket UDP
    client_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    
    # Inicia a thread de escuta
    listener = threading.Thread(target=listen_responses, args=(client_sock,), daemon=True)
    listener.start()

    try:
        # 1. Testar o "JOIN"
        join_payload = {
            "type": "join",
            "name": "PythonTester",
            "id": "test-123"
        }
        print(f"[ENVIO] Enviando 'join' para {SERVER_IP}:{SERVER_PORT}...")
        client_sock.sendto(json.dumps(join_payload).encode('utf-8'), (SERVER_IP, SERVER_PORT))
        
        # Aguarda um pouco para ver o estado inicial
        time.sleep(2)

        # 2. Testar o "INPUT" (Mover para baixo)
        input_payload = {
            "type": "input",
            "id": "test-123",
            "dir": "DOWN"
        }
        print("[ENVIO] Enviando comando de direção 'DOWN'...")
        client_sock.sendto(json.dumps(input_payload).encode('utf-8'), (SERVER_IP, SERVER_PORT))

        # Mantém o script vivo por mais 10 segundos para observar os Ticks do servidor
        time.sleep(10)

    except KeyboardInterrupt:
        print("\nTeste encerrado pelo usuário.")
    finally:
        client_sock.close()

if __name__ == "__main__":
    run_test()