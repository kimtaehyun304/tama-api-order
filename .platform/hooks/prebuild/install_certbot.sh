#!/bin/bash
# Beanstalk EC2 재생성 시 certbot + Let's Encrypt 인증서 자동 설치/갱신
# Nginx reload 실패 시 80포트 점유 프로세스 종료 후 systemctl start

#set -e

# certbot 설치 여부 확인
if [ ! -d /etc/letsencrypt/live ]; then
    echo "Certbot not installed, installing..."
    sudo yum install -y certbot python3-certbot-nginx

    # 인증서 발급
    sudo certbot --nginx \
        -d dldm.kr \
        --email kimapbel@gmail.com \
        --agree-tos \
        --no-eff-email \
        --non-interactive

    # 인증서 갱신 후 Nginx 재시작/시작 스크립트 생성
    sudo tee /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh > /dev/null <<'EOF'
#!/bin/bash

# Nginx reload 시도
if ! sudo systemctl reload nginx; then
    echo "[WARN] Nginx reload failed. killing nginx process..."
    sudo pkill -f nginx
    sudo systemctl start nginx
else
    echo "[SUCCESS] Nginx reloaded successfully."
fi
EOF

    sudo chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
else
    echo "Certbot is already installed, skipping installation."
fi