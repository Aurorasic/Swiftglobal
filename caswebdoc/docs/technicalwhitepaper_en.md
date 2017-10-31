<p >﻿<h1 class="main-title">
    <img src="/img/cas.png"/>
    <span>CAS Technical White Paper</span></h1></p>
<h5 align="center">Version 1.0</h5>

## Chapter I Abstract

This paper mainly introduces the product architecture and technical features of CAS, which are constructed based on the blockchain technology.

The blockchain, as the infrastructure of Bitcoin, is a consensus mechanism in decentralized trustless environment. There are three types of blockchain: public blockchain, consortium blockchain and private blockchain. Consortium blockchain and enterprise private blockchain are used only among consortium members and inside enterprises. 

Public blockchain represent the “value network” attribute at present . According to statistics at www.coinmarketcap.com, the market value of Bitcoin has exceeded USD 75 billion by the time this document is completed, and the market value of Ethereum has exceeded USD 34 billion.

Constructing a trustworthy decentralized system is the core value of blockchain, with the potential of becoming “Value Network” construction facility in the future. CAS is designed to provide a facility platform for financial industry based on the blockchain technology, with the features of distributed storage, high reliability, decentralization, and high performance. It also has capability to allow upper-layer business application to be built up quickly, meeting the requirements of large-scale application scenarios.

## Chapter II Our Objective

The objective of CAS is to construct a trust and value network to serve the financing, trading and payment scenarios, provide third-party financial business with ecological environment to quickly construct applications as well. 

## Chapter III Product Design and Architecture Design

### Detailed Function Description of CAS

CAS is a decentralized blockchain network, which distributes data in different nodes to store them for achieving redundancy. The main functions of CAS system include the following three aspects:

+ Enables a blockchain system with optimized design.

+ Provides the capability of customized, simple, and reliable smart contracts for financial systems.

+ Constructs a democratic, shared-governance, and decentralized economic system.

## Layering and Flat Model of CAS

Both BTC and Ethereum adopt the flat design model, as required by their business functions. The flat model facilitates the implementation of a P2P-based system architecture, which is also a prerequisite for CAS to enable a token system.

The advantage of layered architecture is that it can achieve high cohesion and low coupling in functions, more consistent with open-closed principle. Besides, layered architecture models are more competent in meeting performance demands. Full-data web broadcast itself is a low-efficient yet accurate method, by which information can be accurately synchronized, as long as given sufficient time, while its efficiency is not satisfying.

CAS system needs to support not only the primary demand of value network，but also the complex business needs of upper-layer financial business system. Therefore, a hybrid model has been adopted to design architecture: to implement flat modes in layered architecture. 

#### Overview of the Layered Architecture Design

<img src="/img/cas2.png" > 

As shown in the preceding figure, there are two parts above the general functional platform:

+ The basic service providing layer of CAS platform provides a unified interface set for real business systems.

+ The P2P flat model subsystem will implement the P2P network model internally and invokes core blockchain services through the interface set of VN general functional platform.

Flat models are implemented in layered architecture, which ensures the accuracy of account data synchronization. It also maximizes efficiency of block packing, and designs basic service providing layer for different business systems. 

#### Detailed Description of CAS Software Architecture

#### Overall Architecture

Hybrid model structure in the overall architecture of CAS system:
Main CAS subsystems include Runtime Sandbox, Application Instance, Synchronizer, and Distributed Storage. The following figure is the overview illustration of CAS system. This mode contains not only flat models which runs currency account system, but also multiple financial business constructed on CAS platform. As shown in the figure, all request paths  sent to the low-levellow-layer system are basically the same, in which CT0 is for token system, while CT1 is for actual business scenarios. (Detailed explanations of these key terms will be given in the subsequent terminology list.)

Providing the interface set on VN can support different business forms. Based on the open-designed principle (open source, customized design based on business requirements), RS is of great scalability.

<img src="/img/cas3.png" >

#### Introduction to CAS Main Process

#### Terminology List

|    CAS System Terminology    | Explanation |
| ---------- | --- |
| CAS | Crypto Currency Asset Security |
| RS | Runtime Sandbox |
| VN | Virtual Net |
| AI | Application Instance |
| Slave Node | CAS Slave |
| Synchronizer | Ordering Queue |
| CT | CAS Trade |
| P2P Flat-subsystem | Flat model for P2P network |
| Message Broadcast | Broadcast message to all wallet |

#### Detailed Description of Main Software Subsystems

#### CT

Different business  will be gradually connected to CAS system, and these business  have their own special requirements. The logic of these business  (including the invocation of the general functional interface set providing for the platform) is implemented in CAS Trade.

#### RS 

RS will accommodate a functional node in a certain business. The platform will provide general functional RSs, in order to provide robust compatibility for different business systems. Business individualization support is processed in a customized RS, affiliated to CT.

#### VN

VN is a component designed to implement CT functions, whose main functions are   election finalization, business isolation, and so forth by running smart contracts set in the Policy.

#### Synchronizer

Synchronizer is mainly responsible for achieving transaction instructions packing and the same-type instructions checking.

#### CAS Slave

CAS Slave is the core subsystem of blockchain, implementing logic including UTXO account double-spending verification, block packing. and the persistent storage of all blockchain data.

#### AI

This module is the business API interface set, which is implemented based on different business needs and can facilitate access of new systems or existing systems.

#### P2P Flat-subsystem

This component is provided by CAS platform (an SDK is also provided to facilitate secondary development), and is designed for token systems, which contains the self-implementation P2P network on platform to support message broadcast systems necessary for public blockchains. Besides, CAS system also has multiple built-in message protocols.

#### Message Broadcast

The Message Broadcast system runs on each wallet, whose main purposes are  broadcasting messages and ensure that new messages are sent to other nodes in the Flat-subsystem.

### Overall Flowchart of CAS

#### Main Data Flow of Layered Architecture

<img src="/img/cas4.png" >

#### Brief Introduction to the Data Flow of Layered Architecture

+ A transaction request is sent to the RS through an RS interface invoked by the AI. 

+ Through status advancing, RS sends the request to VN.

+ VN parses the Policy, and runs the smart contract carried in the Policy to complete business voting. 

+ The VN generates a standardized instruction and continues to pass the request to Synchronizer.

+ Synchronizer performs security authentication.

+ Synchronizer generates a package (a set of multiple instructions). 

+ Synchronizer sends the package to Slave.

+ The micro-slave node performs double-spending judgment (multi-nodes parallel voting).

+ The micro-slave node performs block packing.

+ The block is stored in local.

+ The block is broadcast.

+ Synchronizer returns the block to specific VN.

+ VN conceals data according to Policy, then returns them to RS.

+ RS parses data, updates the local ledger, and returns results to AI. AI then invokes the interface provided by the upper-layer business system to return results.

#### Flat Model

Flat model is designed for currency system (including CAS currency and business token). P2P Flat-system is the infrastructure software support module of currency issuance and circulation, mainly implementing the following functions:

+ Implements a P2P network, to support the needs (such as issuance and circulation) of currency (including CAS coins and business tokens) in all wallets connected to this network.

+ Provides low-levellow-layer environmental support for running smart contracts.

+ Implements a set of customized, secure and application-grade message protocols to support message broadcast.

#### P2P Flat-Subsystem Data Flow

<img src="/img/cas5.png" >

#### Overview of the P2P Flat-Subsystem Data Flow

+ A certain wallet creates a transaction (perhaps containing smart contract) and broadcasts it through P2P network.

+ P2P nodes receive the message and broadcast it from one node to another after verifying it successfully.

+ Contract account receives the message, runs contract, and invokes an RS interface to access blockchain system.

+ RS passes tx into VN.

+ VN runs smart contract according to Policy.

+ The message will be sent to core blockchain system (the process here is as same as that of  layered architecture, as described in the preceding sections) to complete double-spending verification and packing, and then be returned.

+ After the transaction is completed, the message will be broadcast.

+ Information is synchronized for all wallet accounts (while there is no need for SPV account to synchronize information).

### CAS Key Technical Design

#### CAS Coins

Blockchain system features decentralization, which means that there is no platform maintainer, and the operation of such a platform is jointly maintained by its participating nodes. To motivate participating nodes to maintain stable platformoperation, there should be some incentives, and the main incentive mechanism of CAS ecological environment is CAS coins.

CAS coins must be valuable in order to encourage nodes to continuously put resources into maintaining healthy platform development. Meanwhile, as CAS ecological system is expanding, CAS coins holders should have the access to benefit from value proceeds, to encourage more participants to join in ecological construction of CAS platform. 

Besides prerequisite of being valuable, CAS coins must also have the payment attribute of  currency.

#### Value Attribute of CAS Coins

CAS coins is an important incentive mechanism of platform operation, whose value is mainly represented in the following aspects:

+ Business on CAS platform demands for CAS coins 

CAS platform can run all third-party financing applications, and provides account, transaction and payment services for these business. CAS network nodes offering these services charge a certain CAS Coins

+ CAS platform users' recognition towards CAS value

Financial applications and financial services, based on CAS platform, are continuously increasing, which will further improve confidence and anticipation from the market and users, CAS coins representing the facility of platform will gain a better price in market as well.

+ CAS Coins holders will gain growing proceeds from the development of CAS ecological system.

By mechanism design, CAS system shares the development dividends of whole ecological system with CAS Coins holders, who will be more inclined to maintain the positive platform development in order to protect their own interests.

#### Payment Function of CAS Coins

CAS Coins is the fundamental account unit and payment unit in CAS ecological system. CAS platform will provide standard wallet interface and wallet client, to enable users conveniently pay by CAS Coins. Payment function shows not only peer-to-peer payment among users, but also decentralized automatic match during order payment by smart contract of CAS network. 

CAS coins are used as commission in the following business :

+ Payment business on CAS platform will charge the initiator a certain amount of CAS coins as commission, which will be shared by CAS account nodes.

+ Decentralized transactions on CAS platform will charge both sides a certain proportion of deal-match commission.

+ Operating smart contract on CAS platform will charge no extra commission.

#### Business Tokens

Business tokens are issued by business side on CAS platform for the purpose of circulation. Different from CAS coins, business tokens cannot be used to pay for the packing costs, the targeted user base is smaller, and they are relevant only with the business side who issues them. On the other hand, business tokens can accurately reflect the value of business and support closed-loop business operation, as a payment method in special business.

#### Value Attribute of Business Tokens

Third-party business can issue tokens to be the payment and account unit of its business-mode operation, and this issuance method will be supported by built-in CAS smart contract. The tokens users bought by smart contract will be recorded on CAS blockchain in the form of digital assets. The business value of well-developed third-party business will be gradually reflected on value of business tokens, and business token holders will benefit from the dividends of  business appreciation.

The circulation process of business tokens on CAS platform is as follows:

<img src="/img/cas7.png" >

The detailed process is described as follows:

+ Business side requests token issuing from CAS platform.

+ CAS network sends initial business tokens to the business side’s wallet.

+ User A initiates a token purchase request to CAS network.

+ CAS network transfers user A's CAS coins to the issuer's wallet.

+ CAS network transfers business tokens from the business side's wallet to user A's wallet.

+ User A initiates a business token transfer request to CAS network.

+ CAS network transfers user A's business tokens to user B's wallet.

#### Business Token Circulation on Transaction Platform

The value attribute of business tokens is also reflected in purchase holding of wider-range institutions and individuals, which requires business tokens to provide standard support for transaction platforms.

To ensure this circulation, the design of business tokens must support ERC20 protocols, so that business tokens can be traded in all major exchanges (all major exchanges support ERC20 protocols). 

#### Payment Attribute of Business Tokens

Besides value attribute, CAS business tokens can be used to pay for fees in business processes, which is called payment attribute. CAS system provides Policy mechanism, allowing third-party to customize flexible pricing rules for distinguishing different business-type charges.

#### Paying for Business Fees

Business sides cannot support continuous business operation for free. In business operation process, they need to put in manpower, materials, and financial resources, such as operating expenses, hardware facilities, and fees paid to CAS packing nodes. To balance their expenditure, business sides may charge business users business tokens to maintain their business operation. CAS platform can support highly-customized payment requests and fees calculation.

#### Regulating Business Tokens’ Usage by Policy

Business sides can specify charging rules by Policy compiling. For example, setting a simple rule,a business charges a business token; During the transaction, when RS implements Policy, it will pay business side the business token owned by users.

If business sides require customization (for example, transfer and cash withdrawal charge different fees), they can make rules in smart contract of Policy, which means transfer and cash withdrawal charge corresponding fees respectively.

#### Account and Balance Design

CAS uses the most widely used account balance recording model. 

#### General Account Balance

In the traditional centralized account design, there are two main account keeping modes: journal account and account book. The former one mainly records day-to-day expenses and balances, whereas the latter one is mainly used to output standard accounting reports. In an informatized system, both these two account keeping modes will rely on databases to persistently store final results eventually. 

#### UTXO

UTXO (Unspent Transaction Output) is the most frequently-used account keeping model in  public blockchain, especially in the field of tokens. UTXO is also one of the most essential designs of BTC as it records the unspent balance, by the approach of transaction as the granularity. That is, the received transaction of one account from one transfer-in account is unique. If it is transferred out again from this address, the newly generated transaction will be used to express. The main advantage of UTXO model is that it makes double-spending checks simpler, thereby each transaction will have a “status” indicates whether it is already spent to show its availability. In a word, each transaction can be spent only once.

#### The Advantages and Disadvantages Comparison of Two Account Balance Recording Modes

All traditional balance recording methods (journal account and account book) will ultimately rely on databases for persistent storage of records. Each user's account record corresponds to a database record. If traditional account keeping methods are used to handle tokens, problems may arise with regard to information concealment. For example, during double-spending checking, if A transfers 20 Yuan to B, a traditional account system must check A's account to make sure that there is at least 20 Yuan available and frozen in its account (that is, subtracting this 20 Yuan from available balance). Therefore, the account system must record all information about A. In such models, any part of the system that handles double-spending verification must record information about all transaction participants. 

UTXO model only needs to reveal a certain transaction ID, and the verification model can tell whether its corresponding balance is available based on checking the transaction status. Therefore, UTXO has a great advantage over double-spending checks. As each transaction can be used only once, by adopting UTXO model, the risk of double-spending can be naturally avoided. Meanwhile, UTXO model does not require all information about A's account, achieving information concealment. 

UTXO solves the problems of double-spending verification and information concealment, but if there are a great number of transactions in one account, it will complicate account handling.

#### CAS Selection

CAS system is required to bear traditional financial business and the most typical business in  existing public blockchain (here mainly including the issuance and circulation of tokens). If a balance account system is used to record data such as balance and day-to-day spending, all relative information is transparent. As for token transactions in public blockchain, information concealment must be guaranteed.

Although balance account model is dominant in traditional business and has withstood the test of time and industries, if this model is used for double-spending verification, it has to store all detailed account information in the low-levellow-layer storage, damaging information concealment, which is unacceptable for public blockchain. Therefore, CAS will use UTXO model to support token business, and RS will use balance account model to support traditional business. Hence, CAS should support the conversion from UTXO model to balance account model, as shown in the following figure:

<img src="/img/cas9.png" >

In addition, too many UTXOs will make it more complicated. CAS will consider using the following methods to reduce available UTXOs in accounts:

+ Combination beforehand: during a transaction, the input is the payer's multiple available UTXOs (the total amount exceeds the amount required for the transaction), and thus available UTXOs in the account will be reduced after the transaction.

+ Combination behindhand: all available UTXOs relating to the account are combined into one after the transaction. That means whenever it is, all accounts have only one available UTXO. In this case, UTXO model is similar to balance account model.

#### CAS Consensus Mining Model

#### Defects of Existing Models

Both BTC and Ethereum use network-wide mining to implement consensus, but this method is low-efficient, which is one of the reasons why it takes BTC 10 minutes to generate a block. Based on the analysis of BTC spread, Ethereum finds that a block can be spread to 95% of the whole network within 12s and therefore sets the time of one block generating as 12s. Even if Ethereum significantly increases the block generation speed based on practical data, the 12s delay may still be unacceptable for actual business systems. 

To resolve the problem of efficiency, network-wide consensus must be replaced by smaller-scope one. Dash coin proposed the Masternodes consensus solution, while Bitshare proposed the DPOS consensus solution. These solutions use token ownership as the proof of rights and interests, and shift network-wide consensus to consensus among token holders. According to  economic theory of POS, if a user in network holds large amounts of tokens, the user himself has no motive to damage the network, for that will result in value loss of his tokens. Therefore, as long as endowing these packing nodes with reasons to hold the tokens, there will be no motive for them to deliberately attempt "forking" operations from the theoretical perspective. This mode also forms the entry threshold, which is reflected in the following two aspects: 

+ Early users can buy large amounts of tokens at relatively low price, whereas late-comers have to pay much more in order to obtain the same rights and interests. For example, at Dash coins’ initial issuing phase, each Dash coin was worth USD 0.21 on February 14, 2014; On August 2017,  the Dash coin price has risen to USD 378.1, about 1766 times higher than the aforementioned initial price. On January 1, 2017, Dash coin price was USD 11.22, about 33 times lower than the price on August 27, 2017.

+ Whether it is Dash coin or Bitshare, participants that only provide resources and do not hold tokens cannot participate in packing incentives allocation, regardless of the resources amount they possess, which also causes that the resources of Bitcoin and Ethereum cannot be directly converted into ones based on POS consensus platform.

CAS uses the DPOS+POW consensus mining model to resolve the problem of low efficiency and to lower the entry threshold of new nodes.

#### Improved DPOS Consensus Algorithm

In order to achieve second-level performance, CAS adopts consensus algorithm DPOS. Any account that holds more than N (to be determined) tokens can apply to become a POS node. POS nodes perform internal shared-governance election (by voting or a method similar to POOL, both of which are voting essentially) to carry out DPOS node selection. The reason for selecting DPOS nodes is to offer rewards for the excellent resources provided by POS nodes. Usually, DPOS node selection criteria are: providing relatively good IDC conditions, dedicated access, and holding large amounts of CAS tokens. Excellent resources can achieve better consensus efficiency and accuracy. Meanwhile, as consensus nodes hold a lot of CAS coins, they are motivated to maintain the accuracy of CAS system. 

For traditional DPOS, one consensus process will only be participated by one node. If the selected DPOS node is a malicious node, temporary forking may occur, and during system operation, the longest chain is used to resolve the forking. This also leads to data unreliability in the first round of consensus process, and data on the abnormal chain may be discarded after several rounds. The solution on Bitcoin and Ethereum is to wait for confirmation by a certain number of subsequent blocks. Only after confirmation by a certain number of subsequent blocks, will it be confirmed that the transaction has been acknowledged or the request has been handled correctly. However, this method makes things more complicated for business access. For business, it may appear that after requests are processed by the blockchain and results are returned, data on the blockchain disappear. If this strategy of Bitcoin or Ethereum is applied, then after DPOS confirmation, business will have to wait for confirmation by a certain number of blocks before proceeding to process results returned by the blockchain (determining whether the returned results are on the primary chain based on the confirmation of subsequent blocks). In that case, the subsequent block generation delay will add to the actual block processing delay. Meanwhile, due to the wait for confirmation by blocks, this logical complexity needs to be handled by business, thereby making business development more complicated and adding the migration and reconstruction costs of existing applications.  

As the issue of data unreliability in the consensus process of traditional DPOS nodes leads to increased business complexity and delay, CAS system makes improvements on traditional DPOS nodes by implementing consistency voting in a round of the consensus process. The consensus process is no longer one node packing and broadcasting. Instead, all DPOS nodes will conduct voting through a consistency algorithm, and after the majority of DPOS nodes reach a consensus, blocks will be returned and be broadcast by all DPOS nodes. After the business system receives the response returned by blockchain, without the necessity of waiting for confirmation by subsequent blocks, it can hold that the data is usually reliable. With the implementation of this majority DPOS node consensus process, the reliability of blockchain is improved, delay reduced, and business access difficulty also reduced. 

#### Block Broadcast Model

Block broadcast adopts a layered broadcast model. After DPOS nodes reach a consensus, block distribution is implemented in two layers:

+ Layer-1 block broadcast distribution: DPOS nodes broadcast blocks to neighboring POS nodes.

+ Layer-2 block broadcast distribution: POS nodes broadcast blocks to non-POS nodes.

Through this type of layered broadcast model, newly generated blocks can be broadcast to network nodes that hold tokens at the fastest speed.

<img src="/img/cas11.png" >

#### Consensus Incentives

After DPOS nodes reach a consensus on packing nodes, there will be consensus incentives. Nodes that reach a consensus in voting will share 70% (to be determined) of the consensus incentives; while DPOS nodes that do not reach a consensus will not be rewarded; other POS nodes share remaining 30% (to be determined) of consensus incentives. Thanks to this incentive mechanism, more nodes will be motivated to become DPOS nodes and to supervise malicious DPOS nodes. POS nodes incentives will encourage them to hold more tokens and provide better resources, which prevents token concentration and allows new DPOS nodes to be generated when malicious nodes appear. Besides, POS nodes can also be mining nodes, to participate in POW mining.

DPOS nodes can punish malicious nodes and nodes with poor resources. For example, if a node casts its vote N (to be determined) consecutive times in discord with the majority votes, DPOS nodes can initiate punitive voting to remove the malicious node by electing a new DPOS node to replace the malicious one. The new DPOS node will be selected from POS nodes. For nodes with poor resources (i.e. nodes where voting times out multiple times or nodes that are slow in response), they can be blacklisted. Blacklisted nodes will have no gains, and if a node is blacklisted multiple times, the node will be removed. This punitive mechanism ensures the correctness and efficiency of DPOS node data and provides excellent consensus services to the whole network.  

#### Mining Incentives

CAS also provides POW mining mechanism. New joined nodes search for HASH-inverse proof-of-work in blocks generated by DPOS. By proof-of-work mechanism, POW ensures that the account results generated by DPOS is tamper-resistant, and in CAS network, POW nodes are  “account witnesses”, while DPOS nodes are “account workers”.

Unlike traditional mining and packing that targets transactions, CAS mining and packing targets blocks generated by DPOS. To ensure that POW can reach a network-wide consensus and that its speed in packing and verifying blocks will not lag far behind DPOS blocks generation, N (to be determined) blocks generated by DPOS are verified by POW at a time, and new blocks are generated based on proof of work. In a high-speed system, multiple DPOS blocks that contain a great number of transactions may cause network transmission problems. Therefore, blocks generated by CAS mining will store the HASH in blocks generated by DPOS. In the light of risks of mine pool concentration, the uncle blocks and nephew blocks in mining will receive small shares of rewards.

#### Smart Contracts

Smart contract is a set of commitments defined in digital forms, including an agreement that allows contract parties to execute these commitments. "Digital form" means that smart contract must be written into computer-readable codes. Once contract parties reach an agreement, the rights and obligations established by smart contracts will be implemented by a computer or by computer network.

Smart contract is program compiled on blockchain. Smart contract, which doesn’t rely on any specific hardware device, is executed by all devices that participate in mining. Code execution should be automatic and atomic: either the execution is successful or all status changes are canceled. In blockchain, this is very important, as it prevents that the contract isn’t executed completely. 

Smart contract is still in an early development stage. Since smart contract was introduced, there has been a great deal of contention on it. Although there are multiple implementation methods, each method has its own different problems. For example, Bitcoin script can only support digital currency transfer, and Ethereum smart contract underwent high-risk loopholes repeatedly, fund losses being up to hundreds of million US dollars.

To avoid these problems, CAS proposed its own smart contract. CAS smart contract is designed in a way that ensures simpleness, practicality and sufficient flexibility and reliability. Unlike Bitcoin script, CAS smart contract not only supports money transfer but also other more complicated business scenarios. CAS smart contract is also different from Ethereum smart contract, as the likelihood of high-risk loopholes in CAS smart contract is lower.

#### Comparison Between CAS Smart Contracts and Other Implementation Methods

#### Why Not Directly Use the Bitcoin Script?

As the first successful blockchain case, Bitcoin has withstood the test of time. Its script is very simple and high-efficient, with very few errors. However, the functions of Bitcoin script are relatively simple, supporting only signature, verification, encryption and hash, while the support for business is relatively weak. Therefore, only simple account transfer can be implemented.

To support more complicated business scenarios and logic, CAS must improve the logical expressiveness of its script by one method. The specific method will be described in subsequent sections.

THE DAO project once raised a large quantity of money through ICO on Ethereum, but due to contract loopholes of recursive calls, the project suffered huge irrecoverable losses, up to USD 150 million. This kind of loss has a very adverse impact on financial systems. 

The reason why this severe loophole occurred was that Ethereum used Turing-complete VMs to execute smart contract. Due to complexity of such VMs, the likelihood of loopholes is relatively high, especially in areas of syntax parsing, complex syntax execution, and business logic compilation. Attackers may take advantage of these loopholes to attack Ethereum by various means just to obtain benefits.

Therefore, CAS platform is very prudent in its selection of smart contract implementation methods. The objective here is to avoid high risks brought by complex VMs and excessive freedom that may lead to errors in business siders' logic compilation. 

#### CAS Smart Contracts

In order to avoid the problems of aforementioned Bitcoin script and Ethereum smart contract, CAS will implement a simple and steady script execution framework, with the syntax similar to JavaScript, but the script does not support complicated syntactical logic. With the syntax simplified and high-risk functions restricted, the script execution will become simple, efficient, and not prone to errors.

However, this brings another problem. CAS smart contracts may not meet the demand of some complicated business scenarios. To solve this problem, CAS will expand its business support aside from smart contract. Specifically, CAS will integrate abundant interfaces on the basic platform to implement some general business logic that cannot be completed by the script. Business siders invoke these reliable interfaces in smart contract to implement complicated business logic. For example, high-risk operations, such as transferring CAS coins, are carried out through interfaces. Interface input will be strictly examined to avoid misappropriation and erroneous deductions. Interface implementation is compiled with mature languages (such as Java) and undergoes strict tests. 

#### CAS Privacy and Isolation Design

As operating business and token platforms on CAS, both privacy and isolation are necessary functions.

#### Business Isolation Requirements

CAS can provide token platform services, which means there will be large numbers of token business running on CAS network. Therefore, business data must be isolated to ensure that each business can only access its relevant data and that each account can only access its own data. CAS meets this demand by the design of  VN, RS and Policy modules.

#### Design of CAS Three-Layer Isolation Mechanism

The three-layer isolation mechanism is designed to meet the different isolation requirements mentioned above.

Design overview:

<img src="/img/cas13.png" >

#### VN Isolation Design

When Slave completes packing blocks and adding them to blockchain, these blocks will be returned layer by layer in the upward direction. The Synchronizer will categorize these blocks based on different business (based on VNid) and send the blocks to the corresponding VNs. Other VNs constructed on CAS cannot receive the blocks. This ensures isolation on the VN layer.

#### RS Isolation Design

The same requirements also goes to the same business, which means the detailed information about a transaction should not be visible to any third parties other than two parties involved in the transaction. When VN sends blocks to RS, it will run smart contract customized in Policy, and the contract will designate transaction data to be sent to certain RS, which can not be seen by third-parties in the same business. This ensures isolation on RS.

#### Account Isolation Design

For two parties in one transaction of the same service, either party can only recognize part of the other party's data. When RS receives the block information returned by VN, RS can only recognize the relevant data of the account that it maintains. Either transaction party can only recognize part of the other party's data, and non-transaction parties cannot recognize each other's data. In this way, account isolation is implemented.

#### Account Isolation Methods- Policy Model Design

+ Description of VN and RS’s  Requirements

Each transaction needs the participation of at least one RS in the corresponding service. When a business RS initiates a transaction request (the request describes a transaction that requires participation by at least two parties), the corresponding Policy should be designated based on the transaction type. Then this Policy will be executed on the VN, so that other RSs can take part in the transaction. Besides, when VN receives blocks returned from lower layers, it also needs to run Policy to determine to which RS it should send the relative data.

+ Policy module design

  Policy functions  mainly include:

  -  Specifies the voting parties and voting method for a transaction.

  - Specifies the return RS node for a packed transaction.

  - Specifies rules of use of business tokens.

+ Policy implement specific functions by smart contract

  - Policy voting participants can be designated through smart contract. For example, actual participants are determined dynamically based on RS status.

  - Policy voting logic can be implemented through smart contract, so that when VN requests RS voting, it is required to execute the contract logic.

  - Policy concealment rules can be implemented through smart contract. Besides basic isolation requirements, individualized concealment requirements can also be implemented. 

  - Usage rules of business tokens of Policy can be implemented through smart 
contract.

+ Policy formulation rights

Based on preceding descriptions to Policy, it can be inferred that different business may require different customized (formulated) Policy.. Therefore, the creation, designation, and modification of one Policy should be maintained by different businesses themselves, and other transaction nodes (RSs) in business can only import relevant Policy, having no right to modify them. Besides, to process different business, business siders' special requirements can be added to Policy. For example, business siders can stipulate that during each business transaction, business sides should use their own business tokens.

## Chapter IV Shared-Governance Community

### Objective of a Shared-Governance Community

CAS, a decentralized platform, without centralized management and control, different from traditional platforms which provide platform maintenance and functional upgrades, its platform adopts the shared-governance community model for its maintenance. In this model, all participating nodes take charge of the healthy development of their community, and the whole community vote on development proposals. Resolutions passed will be implemented, thus achieving the goal of ecological shared governance and joint management of the community. A healthy community environment is essential for healthy platform development and creating a more vibrant platform.

The shared-governance community mainly composes of proposal submitters, CAS Joint Committee, proposal voters, and resolution executors. This paper mainly discuss technical theory in voting process, so please refer to “CAS Business White Paper” for detailed CAS network voting resolution mechanism 

In proposal voting process, holders of CAS Coins can cast votes, and passed proposals are called community resolutions, which will be implemented by CAS initiating team launches new-version CAS codes. All network nodes are resolution executors. They must accept and implement resolutions. For nodes that refuse to implement resolutions, their request will be rejected by network.  

Shared community governance and healthy platform development can be achieved by  shared-governance resolution process.

### CIP (CAS Improvement Proposals) Voting

CAS community will endow eligible community members with equal rights to determine the developing direction of community, which is exactly the goal of shared-governance community, and voting function on platform ensures its complement.

#### CAS Community Voting Rights

Community allocates voting rights based on the amount of CAS coins that each account holds. The allocation is in accordance with the principle of "one coin, one vote" and "same coins, same rights". "one coin, multiple votes" is strictly prohibited in design. For example, if account A holds 10 CAS coins, account A has 10 votes. Then, Account A is prohibited from transferring its CAS coins to account B. The procedure is as follows:

+ Voting rights allocation is implemented based on smart contract designated by one platform. During this process, it should be ensured that each eligible account receives the message and runs the contract, so that votes pertaining to a specific resolution are generated for these user accounts. Votes will be in one-to-one mapping with credentials (such as txID). Meanwhile, votes will be written into blockchain. 

+ Once used, votes will be invalidated. This principle is consistent with UTXO in theory. Vote usage is also based on smart contract.

#### Voting Process

If proposal submitters want to optimize CAS ecology, they need to reach a consensus in community by voting process to implement resolutions. Consensuses reached by voting within a small scope will not be recognized by community. Only the number of voters reach the required threshold, can the voting result take effect, On the other hand, the community does not allow malicious participants to take part in voting without costs. Therefore, it sets thresholds for participators to improve the quality of voting.  

To implement aforementioned rules, community sets ground rules for voting process, such as restrictions on the number of voters, approval and the upper and lower limits of voting time. Proposals themselves also make rules on voting process and may even have stricter restrictions on the number of voters. These ground rules are enforced by smart contact.

Valid votes will be broadcast in the whole community, and users who are eligible to vote will receive vote content on a distributed browser. Voting will be simplified into the format of approval and disapproval, so that users can make choices conveniently. Once users make their choices, the choices will be reported to community and recorded.

When the voting period is over, if the number of voters and approval reach the thresholds set by platform and relevant proposals, approved proposals will be checked by smart contract and be converted into the final resolution, recorded on,,community agenda. The resolution will be executed by smart contract.

### Shared-Governance Community Incentives

Any effective resolution by shared-governance community is the result of a process where eligible voters in the community independently cast their votes in accordance with certain rules. This result has an influence on everyone.

## Chapter V CAS Reliability, Security, Performance, Deployment and O&M Design

### CAS Reliability Analysis

RS, VN, Synchronizer and CAS Slave nodes can be maintained by individuals or other institutional maintainers. Theoretically speaking, RS and VN can just both be one node at minimum, but clearly that it is not sufficient for high-availability service, which requires RS and VN with multiple nodes.

Synchronizer and CAS Slave consist of multiple nodes and provide high-availability services externally. By using a specially optimized consistent module, CAS Slave can bear no-more-than-51% nodes offline or data lost. CAS platform has designed a failover process to handle scenarios where nodes get offline or data get lost. Besides, data recovery service for problems caused by bugs in upper-layer business systems are also supported. The failover of RS, VN and CAS Slave does not affect normal operations.

### CAS Performance Analysis and Actual Performance Data

According to the tested data fromour test system( more than 1,000 CAS Slave nodes), RS and VN are both deployed on VM nodes,  which can maintain the average delay at seconds when TPS exceeds 10,000.

To acquire enough network test nodes, we run docker container on VM(virtual machine). The largest-scale simulation contains 1,000 network nodes, and by implementing pressure tests on different-scale network node quantity, the acquired data of tps and latency is as follows:

|    Network Node Quantity    | tps | latency（s）|
| ---------- | --- | --- |
| 3 | 138363 | 0.23 |
| 5 | 136363 | 0.23 |
| 7 | 135363 | 0.25 |
| 13 | 125363 | 0.33 |
| 21 | 125063 | 0.37 |
| 49 | 	123054 | 	0.53 | 
| 99 | 	106307 | 	0.83 | 
| 149 | 	100432 | 	1.12 | 
| 199 | 	89643 | 	1.39 | 
| 249 | 	80157 | 	1.66 | 
| 299 | 	75189 | 	1.9 | 
| 349 | 	65954 | 	2.14 | 
| 399 | 	58669 | 	2.35 | 
| 449 | 	49549 | 	2.55 | 
| 499 | 	39810 | 	2.73 | 
| 549 | 	30666 | 	2.89 | 
| 599 | 	22335 | 	3.04 | 
| 649 | 	18031 | 	3.16 | 
| 699 | 	14013 | 	3.26 | 
| 749 | 	14001 | 	3.34 | 
| 799 | 	13631 | 	3.4 | 
| 849 | 	13331 | 	3.43 | 
| 899 | 	13337 | 	3.43 | 
| 949 | 	12971 | 	3.42 | 
| 999 | 	13092 | 	3.37 | 

<img src="/img/cas17.png" >

<img src="/img/cas18.png" >

According to data and tendency on the diagram, we can learn that when the network node quantity reaches around 700, tps and latency tends to be stable without the influence of node quantity.

When nodes grow, CAS consensus algorithm needs more votes from them, and latency will increase for network cost increases at the same time. From pressure tests, we find that when network scale exceeds 700, latency tends to be stable. The reason is that in this scale, network broadcast cost tends to be constant, link among nodes achieves efficient broadcast and tps also tends to be stable.

### System Deployment and System O&M

System deployment: To facilitate deployment, we have a set of specialized deployment scripts for RS, VN and CAS Slave. The deployment scripts are subject to open sourcing along with all CAS open source codes.

System O&M: the basic monitoring and business monitoring systems of CAS are provided in open-source mode. The open-sourcing of RS and VN allows business system owners to modify the monitoring systems based on their own requirements. Besides, we provide troubleshooting tools to handle failover and implement fault isolation. These tools will also be subject to open-sourcing. 

### VN, RS and CAS Slave Upgrades and Capacity Expansion

CAS is a distributed system. Therefore, RS, VN, and CAS Slave all need online upgrades and capacity expansion. To achieve them, CAS platform has completed the development of relevant tools and formulated upgrades and capacity expansion standards. These tools can be used to complete upgrades and capacity expansion by simple manual procedure based on relevant standards.






