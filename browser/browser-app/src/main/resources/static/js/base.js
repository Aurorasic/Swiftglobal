var browser_api = {
    URL: {
        getTransactionByHash: function (transactionHash) {
            return "/v1.0.0/transactions/info?hash=" + transactionHash;
        },
        getTransactionByPubKey: function (pubKey, op) {
            return "/v1.0.0/transactions/list?pubKey=" + pubKey + "&op=" + op;
        },
        getBlockByHash: function (blockHash) {
            return "/v1.0.0/blocks/info?hash=" + blockHash;
        },
        getBlockHeaderByHash: function (blockHash) {
            return "/v1.0.0/blocks/header?hash=" + blockHash;
        },
        getBlockHeaderList: function (start, limit) {
            return "/v1.0.0/blocks/headerList?start=" + start + "&limit=" + limit;
        },
        getRecentHeaderList: function (limit) {
            return "/v1.0.0/blocks/recentHeaderList?limit=" + limit;
        },
        getMinerCount: function () {
            return "/v1.0.0/miners/count";
        },
        isMinerByPubKey: function (pubKey) {
            return "/v1.0.0/miners/isMiner?pubKey=" + pubKey;
        },
        getBlockByMinerPubKey: function (pubKey) {
            return "/v1.0.0/miners/blocks?pubKey=" + pubKey;
        },
        getUTXOByAddress: function (address) {
            return "/v1.0.0/utxos/utxo?address=" + address;
        }
    }
};