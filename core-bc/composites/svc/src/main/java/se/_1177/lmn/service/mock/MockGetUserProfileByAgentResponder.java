package se._1177.lmn.service.mock;

import mvk.itintegration.userprofile._2.RelativeListType;
import mvk.itintegration.userprofile._2.RelativeType;
import mvk.itintegration.userprofile._2.RelativeTypeType;
import mvk.itintegration.userprofile._2.ResultCodeEnum;
import mvk.itintegration.userprofile._2.UserProfileType;
import mvk.itintegration.userprofile.getuserprofilebyagent._2.rivtabp21.GetUserProfileByAgentResponderInterface;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentResponseType;
import mvk.itintegration.userprofile.getuserprofilebyagentresponder._2.GetUserProfileByAgentType;

import javax.jws.WebService;

/**
 * @author Patrik Björk
 */
@WebService(targetNamespace = "urn:mvk:itintegration:userprofile:GetUserProfileByAgent:2:rivtabp21'", name = "GetUserProfileByAgentResponderInterface")
public class MockGetUserProfileByAgentResponder implements GetUserProfileByAgentResponderInterface {

    @Override
    public GetUserProfileByAgentResponseType getUserProfileByAgent(GetUserProfileByAgentType request) {

        RelativeType relative = new RelativeType();
        relative.setSubjectOfCareId(request.getSubjectOfCareId());
        relative.setRelativeType(RelativeTypeType.PARENT);

        RelativeListType relatives = new RelativeListType();
        relatives.getItems().add(relative);

        UserProfileType userProfile = new UserProfileType();
        userProfile.setEmail("dummy.email@example.com");
        userProfile.setCity("Göteborg");
        userProfile.setMobilePhoneNumber("0701234567");
        userProfile.setFirstName("Vilde");
        userProfile.setLastName("Barnsson");
        userProfile.setPhoneNumber("031-123456");
        userProfile.setStreetAddress("Gatan 47");
        userProfile.setZip("42137");
        userProfile.setRelatives(relatives);

        GetUserProfileByAgentResponseType response = new GetUserProfileByAgentResponseType();

        response.setAgentName("Testförälder Testsson");
        response.setCurrentUserProfile(userProfile);
        response.setResultCode(ResultCodeEnum.OK);

        return response;
    }
}
