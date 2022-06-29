# Small Bank with Spring Boot

This repository demonstrates the small bank interacting with H2 database and Ganache blockchain.

## Functionality requirements

- [x] User registration
- [x] Account creation (wallet)
- [x] Login
- [x] Realization of money deposit
- [x] List of accounts with a balance
- [x] List of transactions
- [x] Transfer ether from account A to account B
- [x] Deposit/Withdraw ether from/to blockchain
- [x] JWT authentication

## Pre-requirements

- Run Ganache by CLI or GUI
- Update Ganache RPC in `application.yml`

## REST APIs

### User registration

#### Request

Method: `POST`

URL: `/v1/bank/create-account`

Content-Type: `application/json`

Body:

```json
{
  "name": "name",
  "password": "password"
}
```

#### Response

```json
{
  "code": "SUCCESS",
  "data": {
    "address": "0x8a018c0dc8ff0d1256f8a3fb83edb9bd3bfb97fe"
  },
  "time": "2022-06-27 10:06:41.004"
}
```

### Login

#### Request

Method: `POST`

URL: `/users/login`

Content-Type: `application/json`

Body:

```json
{
  "name": "name",
  "password": "password"
}
```

#### Response

Response Header
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJuYW1lIiwiZXhwIjoxNjU3Mzc0MDE5fQ.3Y06--LYsRxys6p9X8a1y8ao1nxkdeHLHagB-8sYm6-T5N2Z6NO9hkj6QFPTv8V4c-akJ28PnFCe7ydMD_4Jug
```


### Get balance of account

Get the balance of account in Ganache

#### Request

Method: GET

URL: `/v1/bank/{address}`

Content-Type: `application/json`

Authorization: `Bearer ****`

Body:

```json
{
  "name": "name",
  "password": "password"
}
```

#### Response

```json
{
  "code": "SUCCESS",
  "data": "3.875178109684212736",
  "time": "2022-06-27 10:00:16.004"
}
```

### Transfer ether from account A to account B

Account A and B should be added to the bank

#### Request

Method: `GET`

URL: `/v1/bank/transfer?to={to address}&amount={amount}`

Content-Type: `application/json`

Authorization: `Bearer ****`

Body:

```json
{
  "name": "name",
  "password": "password"
}
```

#### Response

```json
{
  "code": "SUCCESS",
  "data": {
    "txHash": "f46402659e5a02c6e8536b0cf88a966d"
  },
  "time": "2022-06-27 10:08:50.004"
}
```

### Deposit

Deposit ether from the blockchain to the bank. Only the registered account can deposit.

The bank will automatically realize the deposit transaction and update the balance in the bank.

To deposit ether, the account should have ether balances in the blockchain.

```
curl http://127.0.0.1:8545 -X POST -H "Content-Type: application/json"  -d '{"jsonrpc":"2.0","method":"eth_sendTransaction","params":[{"from": "ganache account", "to": "account wallet address in bank", "value": "0x35C9ADC5DEA00000"}],"id":1}'
```

#### Request

Method: `GET`

URL: `/v1/bank/deposit?amount={amount}`

Content-Type: `application/json`

Authorization: `Bearer ****`

#### Response

```json
{
  "code": "SUCCESS",
  "data": {
    "txHash": "0x99f8211c03c3fc9f03ba3a8c495dcef70aee41eba2587f2c4a227abc9eca4d48"
  },
  "time": "2022-06-27 10:07:01.004"
}
```

### Withdraw

Withdraw ether from the bank to the blockchain. Only the account which has ether balance in the bank can withdraw.

#### Request

Method: `GET`

URL: `/v1/bank/withdraw?amount={amount}`

Content-Type: `application/json`

Authorization: `Bearer ****`

#### Response

```json
{
  "code": "SUCCESS",
  "data": {
    "txHash": "0xad10726d58c1ed86169da99e9615a7f384b29d0b5ef8a59fed9fa968ec8b537d"
  },
  "time": "2022-06-27 10:09:57.004"
}
```
