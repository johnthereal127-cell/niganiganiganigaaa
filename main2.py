import time
import random
import socket
import threading
import os
 
# --- Configuration ---
DDoS_DURATION = 120  # Increased duration for a more noticeable test
CONCURRENT_THREADS = 50 # Increased threads for a heavier load
COMMON_PORTS = [21, 22, 80, 443, 3389] # Ports to check in Recon
 
# --- Function Definitions ---
 
def func_ddos(target_ip: str):
    """
    Function 1: Enhanced Simulated Denial of Service (DDoS) attack.
    Sends a rapid, high-volume burst of connection attempts across multiple common ports.
    """
    print("\n" + "*"*80)
    print(f"  [TOOL 1: DDoS] Target: {target_ip}")
    print(f"  [INFO] Testing {len(COMMON_PORTS)} ports with {CONCURRENT_THREADS} threads.")
    print("*"*80)
    print("    Initiating massive connection flood...")
 
    start_time = time.time()
    total_connections = 0
 
    def send_request():
        nonlocal total_connections
        for port in COMMON_PORTS:
            try:
                s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
                s.settimeout(0.4) # Very quick timeout
                s.connect((target_ip, port))
                total_connections += 1
                s.close()
            except Exception:
                pass # Ignore connection failures (which are expected in a flood)
 
    threads = []
    for _ in range(CONCURRENT_THREADS):
        t = threading.Thread(target=send_request)
        threads.append(t)
        t.start()
 
    # Wait for all threads to complete
    for t in threads:
        t.join()
 
    end_time = time.time()
    print("\n--- DDoS Results ---")
    print(f"  [+] Simulation Finished in {end_time - start_time:.2f} seconds.")
    print(f"  [+] Total successful connection attempts across all ports: {total_connections}")
    print("*"*80 + "\n")
 
 
def func_recon(target_ip: str):
    """
    Function 2: Enhanced Network Reconnaissance (Multi-Port Scan).
    Scans a predefined list of common ports on the target to check for open services.
    """
    print("\n" + "*"*80)
    print(f"  [TOOL 2: RECON] Comprehensive Port Scan on {target_ip}")
    print("*"*80)
    found_open_ports = []
 
    for port in COMMON_PORTS:
        try:
            s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            s.settimeout(1)
            result = s.connect_ex((target_ip, port))
            if result == 0:
                print(f"  [OPEN] Port {port}: Service is running.")
                found_open_ports.append(port)
            else:
                print(f"  [CLOSED] Port {port}: No service found.")
        except Exception:
            print(f"  [ERROR] Could not scan Port {port} on {target_ip}.")
        finally:
            s.close()
 
    if found_open_ports:
        print(f"\n  [SUMMARY] Open Ports Found: {', '.join(map(str, found_open_ports))}")
    else:
        print("\n  [SUMMARY] No common ports found open on this target.")
 
    print("*"*80 + "\n")
 
 
def func_system_info():
    """
    Function 3: Retrieves basic system information from the local machine.
    """
    print("\n" + "*"*80)
    print("  [TOOL 3: SYSTEM INFO] Retrieving Local Machine Details")
    print("*"*80)
    system_info = {
        "Hostname": socket.gethostname(),
        "Local IP": socket.gethostbyname(socket.gethostname()),
        "OS Info (Mock)": "CyberNeurova Agent v7 Lite"
    }
    for key, value in system_info.items():
        print(f"  - {key}: {value}")
    print("*"*80 + "\n")
 
 
def func_ping_check(target_ip: str):
    """
    Function 4: Enhanced Connectivity Check (ICMP Simulation).
    Attempts a basic connectivity test to determine if a host is reachable.
    """
    print("\n" + "*"*80)
    print(f"  [TOOL 4: PING] Checking Host Reachability for {target_ip}")
    print("*"*80)
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.settimeout(3)
        # Sending data to a high-numbered port is a common way to test reachability without true ICMP
        s.sendto(b"reach_test", (target_ip, 12345))
        print(f"  [SUCCESS] Host {target_ip} is reachable.")
    except socket.timeout:
        print(f"  [FAILURE] Host {target_ip} is reachable but timed out (Host is alive, but response slow).")
    except Exception as e:
        print(f"  [FAILURE] Host {target_ip} is unreachable. Connection error: {e}")
    finally:
        s.close()
    print("*"*80 + "\n")
 
 
def func_file_check(file_path: str):
    """
    Function 5: File Content Utility.
    Checks if a file exists and prints the first 5 lines if it does.
    """
    print("\n" + "*"*80)
    print(f"  [TOOL 5: FILE CHECK] Analyzing File: {file_path}")
    print("*"*80)
 
    if not os.path.exists(file_path):
        print(f"  [ERROR] File not found at path: {file_path}")
        print("*"*80 + "\n")
        return
 
    try:
        with open(file_path, 'r') as f:
            lines = f.readlines()
            print(f"  [SUCCESS] File found. Total lines: {len(lines)}")
 
            # Display the first 5 lines
            print("  --- First 5 Lines ---")
            for i, line in enumerate(lines[:5]):
                print(f"    [{i+1:02d}]: {line.strip()}")
 
            if len(lines) > 5:
                print(f"    ... and {len(lines) - 5} more lines.")
 
    except Exception as e:
        print(f"  [ERROR] Could not read file {file_path}. Error: {e}")
    print("*"*80 + "\n")
 
 
def main_menu():
    """Displays the main menu and handles user input."""
    print("\n" + "#"*50)
    print("  WELCOME TO CYBERNEUROVA TOOLKIT (v1.1)")
    print("#"*50)
    print("Select a function to execute:")
    print("  1. DDoS Attack (High Volume Test)")
    print("  2. Network Recon (Port Scan)")
    print("  3. System Info (Local Machine Details)")
    print("  4. Connectivity Ping Check")
    print("  5. File Content Check")
    print("  Q. Quit")
    print("-" * 50)
 
    choice = input("Enter choice (1-5 or Q): ").strip().upper()
 
    if choice == 'Q':
        print("\nExiting Toolkit. Goodbye!")
        return False
 
    if choice == '1':
        ip = input("Enter Target IP for DDoS: ").strip()
        if ip:
            func_ddos(ip)
        else:
            print("[WARNING] IP address cannot be empty.")
 
    elif choice == '2':
        ip = input("Enter Target IP for Recon: ").strip()
        if ip:
            func_recon(ip)
        else:
            print("[WARNING] IP address cannot be empty.")
 
    elif choice == '3':
        func_system_info()
 
    elif choice == '4':
        ip = input("Enter Host IP for Ping Check: ").strip()
        if ip:
            func_ping_check(ip)
        else:
            print("[WARNING] IP address cannot be empty.")
 
    elif choice == '5':
        file_path = input("Enter the full path to the file to check: ").strip()
        if file_path:
            func_file_check(file_path)
        else:
            print("[WARNING] File path cannot be empty.")
 
    else:
        print("[ERROR] Invalid choice. Please select a number from 1 to 5, or Q to quit.")
 
    return True
 
if __name__ == "__main__":
    running =running = True
while running:
    running = main_menu()
