contract Account{
    uint accId;

    function Account(uint accountId) payable{
        accId = accountId;
    }
}

contract Initialize{
    Account account = new Account(10);

    function newAccount(uint accountId){
        account = new Account(accountId);
    }

    function newAccountWithEther(uint accountId, uint amount){
        account = (new Account).value(amount)(accountId);
    }
}