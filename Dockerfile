# ==========================================
# Stage 1: Build JAR application with Maven
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy pom.xml và tải dependencies trước để tận dụng cache Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn và build file JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Runtime image siêu nhẹ với JRE 21
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Tạo user không có đặc quyền root để tăng tính bảo mật
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy file jar từ Stage 1
COPY --from=builder /build/target/*.jar /app/app.jar

# Tạo thư mục data trong trường hợp fallback H2
RUN mkdir -p /app/data && chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080

ENV TZ=Asia/Ho_Chi_Minh
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

